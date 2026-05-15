CREATE TABLE IF NOT EXISTS products
(
    id          UUID                        NOT NULL DEFAULT gen_random_uuid(),
    name        VARCHAR(255)                NOT NULL,
    price       NUMERIC(19, 4)              NOT NULL,
    created_at  TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP WITH TIME ZONE    NOT NULL DEFAULT now(),
    CONSTRAINT  pk_products                 PRIMARY KEY (id)
);
