FROM postgres:17

# 미리 컴파일된 파일들 복사
COPY vector/vector.so /usr/lib/postgresql/17/lib/
COPY vector/vector.control /usr/share/postgresql/17/extension/
COPY vector/vector--0.5.1.sql /usr/share/postgresql/17/extension/

# 초기화 SQL 복사
COPY manulo.sql /docker-entrypoint-initdb.d/