INSERT INTO products (name, price)
SELECT name, price
FROM (VALUES
          ('po-1',  10.0000),
          ('po-2',  20.0000),
          ('po-3',  30.0000),
          ('po-4',  40.0000),
          ('po-5',  50.0000),
          ('po-6',  60.0000),
          ('po-7',  70.0000),
          ('po-8',  80.0000),
          ('po-9',  90.0000),
          ('po-10', 100.0000)
     ) AS seed(name, price)
WHERE NOT EXISTS (SELECT 1 FROM products);
