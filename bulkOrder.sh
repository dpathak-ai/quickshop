for i in {1..10}; do
  DATA="{\"partition\":0,\"key\":null,\"content\":\"{\\n  \\\"orderId\\\": $i,\\n  \\\"productId\\\": 1,\\n  \\\"quantity\\\": 1\\n}\",\"keySerde\":\"String\",\"valueSerde\":\"String\"}"
  curl --location 'http://localhost:9000/api/clusters/quickshop/topics/order.placed/messages' \
    --header 'Content-Type: application/json' \
    --header 'Cookie: JSESSIONID=27A6CD01D40784E65157EEA7A51062DA' \
    --data "$DATA"
done





