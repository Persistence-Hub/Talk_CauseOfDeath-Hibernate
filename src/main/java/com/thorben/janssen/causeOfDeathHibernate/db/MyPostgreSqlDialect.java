package com.thorben.janssen.causeOfDeathHibernate.db;

import org.hibernate.dialect.PostgreSQL10Dialect;
import org.hibernate.dialect.function.StandardSQLFunction;

public class MyPostgreSqlDialect extends PostgreSQL10Dialect {

    public MyPostgreSqlDialect() {
        super();
        registerFunction("STRING_AGG", new StandardSQLFunction("STRING_AGG"));
    }
}