-- 1. 먼저 vector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. 기본 테이블 생성 (외래 키 제약 없이)
CREATE TABLE member (
  member_id SERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password VARCHAR(255),
  name VARCHAR(20) NOT NULL,
  role VARCHAR(10) DEFAULT 'USER' NOT NULL CHECK (role IN ('USER', 'ADMIN')),
  provider VARCHAR(255),
  provider_id VARCHAR(255),
  join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

CREATE TABLE category (
  category_id SERIAL PRIMARY KEY,
  category_name VARCHAR(50) NOT NULL,
  category_icon VARCHAR(255) NOT NULL
);

CREATE TABLE product (
  product_id SERIAL PRIMARY KEY,
  category_id INTEGER NOT NULL,
  model_name VARCHAR(255) NOT NULL,
  model_image VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  feature_summary TEXT NOT NULL,
  feature_embedding vector(1536) NOT NULL
);

CREATE TABLE favorite (
  favorite_id SERIAL PRIMARY KEY,
  product_id INTEGER NOT NULL,
  member_id INTEGER NOT NULL
);

CREATE TABLE query_history (
  query_id SERIAL PRIMARY KEY,
  product_id INTEGER NOT NULL,
  query_text VARCHAR(255) NOT NULL,
  response_text TEXT NOT NULL,
  query_time BIGINT NOT NULL,
  member_id INTEGER NOT NULL
);

CREATE TABLE prompt_templates (
  id VARCHAR(255) PRIMARY KEY,
  template TEXT NOT NULL,
  last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
  description VARCHAR(255),
  active BOOLEAN DEFAULT TRUE NOT NULL
);

CREATE TABLE notifications (
  id SERIAL PRIMARY KEY,
  member_id INTEGER NOT NULL,
  message TEXT NOT NULL,
  title VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
);

-- 3. 그 다음 외래 키 제약 추가
ALTER TABLE favorite
  ADD CONSTRAINT fk_favorite_product FOREIGN KEY (product_id) REFERENCES product(product_id),
  ADD CONSTRAINT fk_favorite_member FOREIGN KEY (member_id) REFERENCES member(member_id);

ALTER TABLE query_history
  ADD CONSTRAINT fk_query_product FOREIGN KEY (product_id) REFERENCES product(product_id),
  ADD CONSTRAINT fk_query_member FOREIGN KEY (member_id) REFERENCES member(member_id);

ALTER TABLE product
  ADD CONSTRAINT fk_product_category FOREIGN KEY (category_id) REFERENCES category(category_id);

ALTER TABLE notifications
    ADD CONSTRAINT fk_notification_member FOREIGN KEY (member_id) REFERENCES member(member_id);

-- 4. 인덱스 추가 (조회 성능 향상)
CREATE INDEX idx_notification_member_id ON notifications(member_id);
CREATE INDEX idx_notification_created_at ON notifications(created_at);

-- 5. category 데이터 삽입
INSERT INTO public.category (category_name, category_icon)
VALUES
    ('tv','https://cdn-icons-png.flaticon.com/128/10811/10811514.png'),
    ('refrigerator','https://cdn-icons-png.flaticon.com/128/2969/2969229.png'),
    ('washing_machine','https://cdn-icons-png.flaticon.com/128/75/75258.png'),
    ('microwave','https://cdn-icons-png.flaticon.com/128/508/508620.png'),
    ('air_conditioner','https://cdn-icons-png.flaticon.com/128/863/863923.png'),
    ('vacuum','https://cdn-icons-png.flaticon.com/128/4917/4917553.png'),
    ('water_purifier','https://cdn-icons-png.flaticon.com/128/15512/15512166.png'),
    ('coffee_machine','https://cdn-icons-png.flaticon.com/128/13888/13888309.png'),
    ('rice_cooker','https://cdn-icons-png.flaticon.com/128/1670/1670652.png'),
    ('smartphone','https://cdn-icons-png.flaticon.com/128/15/15874.png'),
    ('tablet','https://cdn-icons-png.flaticon.com/128/25/25466.png'),
    ('laptop','https://cdn-icons-png.flaticon.com/128/689/689396.png'),
    ('smartwatch','https://cdn-icons-png.flaticon.com/128/6421/6421054.png'),
    ('earphone','https://cdn-icons-png.flaticon.com/128/5906/5906124.png'),
    ('ebook_reader','https://cdn-icons-png.flaticon.com/128/18418/18418628.png');

-- Table: public.langchain_pg_collection

-- DROP TABLE IF EXISTS public.langchain_pg_collection;

CREATE TABLE IF NOT EXISTS public.langchain_pg_collection
(
    uuid uuid NOT NULL,
    name character varying(255) COLLATE pg_catalog."default" NOT NULL,
    cmetadata jsonb,
    CONSTRAINT langchain_pg_collection_pkey PRIMARY KEY (uuid)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.langchain_pg_collection
    OWNER to postgres;

-- Table: public.langchain_pg_embedding

-- DROP TABLE IF EXISTS public.langchain_pg_embedding;

CREATE TABLE IF NOT EXISTS public.langchain_pg_embedding
(
    id BIGSERIAL PRIMARY KEY,
    collection_id uuid NOT NULL,
    custom_id text COLLATE pg_catalog."default",
    document text COLLATE pg_catalog."default" NOT NULL,
    cmetadata jsonb,
    embedding vector(1536) NOT NULL,
    CONSTRAINT fkrbrh7bjxp7ym4rf6xa2fqfjqg FOREIGN KEY (collection_id)
        REFERENCES public.langchain_pg_collection (uuid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.langchain_pg_embedding
    OWNER to postgres;
