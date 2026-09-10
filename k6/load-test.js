import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Two independent load profiles running at once: a big crowd of browsers
// (list/detail/inventory reads - all public, no auth) and a smaller trickle
// of buyers (authenticated order creation). Ratio mirrors real storefronts
// where most traffic is browsing and only a fraction converts to a purchase.
export const options = {
  scenarios: {
    browsing: {
      executor: 'ramping-vus',
      exec: 'browse',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 20 },
        { duration: '2m', target: 20 },
        { duration: '30s', target: 60 },
        { duration: '2m', target: 60 },
        { duration: '30s', target: 0 },
      ],
    },
    ordering: {
      executor: 'ramping-vus',
      exec: 'placeOrder',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 3 },
        { duration: '2m', target: 3 },
        { duration: '30s', target: 10 },
        { duration: '2m', target: 10 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_duration: ['p(95)<800'],
    http_req_failed: ['rate<0.02'],
  },
};

export function setup() {
  const email = `loadtest_${Date.now()}@quickshop.test`;
  const password = 'LoadTest123!';

  const registerRes = http.post(
    `${BASE_URL}/api/v1/auth/register`,
    JSON.stringify({ name: 'Load Test User', email, password }),
    { headers: { 'Content-Type': 'application/json' } }
  );

  let token;
  if (registerRes.status === 201) {
    token = registerRes.json('accessToken');
  } else {
    const loginRes = http.post(
      `${BASE_URL}/api/v1/auth/login`,
      JSON.stringify({ email, password }),
      { headers: { 'Content-Type': 'application/json' } }
    );
    token = loginRes.json('accessToken');
  }

  const productsRes = http.get(`${BASE_URL}/api/v1/products?page=0&size=20`);
  const productIds = productsRes.json('content').map((p) => p.id);

  return { token, productIds };
}

function randomProductId(data) {
  return data.productIds[Math.floor(Math.random() * data.productIds.length)];
}

export function browse(data) {
  const list = http.get(`${BASE_URL}/api/v1/products?page=0&size=10`, { tags: { name: 'ProductList' } });
  check(list, { 'list status 200': (r) => r.status === 200 });

  const productId = randomProductId(data);

  const detail = http.get(`${BASE_URL}/api/v1/products/${productId}`, { tags: { name: 'ProductDetail' } });
  check(detail, { 'detail status 200': (r) => r.status === 200 });

  const inv = http.get(`${BASE_URL}/api/v1/inventory/${productId}`, { tags: { name: 'InventoryCheck' } });
  check(inv, { 'inventory status 200': (r) => r.status === 200 });

  sleep(1);
}

export function placeOrder(data) {
  const productId = randomProductId(data);

  const res = http.post(
    `${BASE_URL}/api/v1/orders`,
    JSON.stringify({ productId, quantity: 1 }),
    {
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${data.token}`,
      },
      tags: { name: 'CreateOrder' },
    }
  );

  // 202 = accepted; 400 here is usually stock depletion (a real business
  // rule, not a service failure) once a run has placed enough orders.
  check(res, { 'order accepted or out of stock': (r) => r.status === 202 || r.status === 400 });

  sleep(2);
}
