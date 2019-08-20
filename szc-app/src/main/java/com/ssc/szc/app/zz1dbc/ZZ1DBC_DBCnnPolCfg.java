package com.ssc.szc.app.zz1dbc;

public class ZZ1DBC_DBCnnPolCfg {

    private static final String NO = "N";

    private static final String YES = "Y";

    /** 数据库分库名称 */
    private String dbDivName = null;

    /** 最小数据库连接数 */
    private int minSize = 0;

    /** 最大数据库连接数 */
    private int maxSize = 0;

    /** 必须保留的最小空闲连接数阀值 */
    private int idleThreshold = 0;

    /** 创建空闲连接时，一次创建的连接数 */
    private int additionalCount = 0;

    /** 数据库连接创建后多久被回收，单位为毫秒 */
    private long recycleTimeMS = 0;

    /** 连接被使用多少次后废弃 */
    private int recycleCount = 0;

    /** 数据库连接自动提交事务 */
    private boolean autoCommit;

    /** 数据库连接事务超时时间，单位为毫秒 */
    private long transactionTimeoutMS = 0;

    /** 新建连接以及校验连接有效性的超时时间，单位为秒 */
    private int connectionTimeoutSeconds = 0;

    /** 取数据库连接的最大等待时间，单位为毫秒 */
    private long retrieveTimeoutMS = 0;

    /** 使用连接之前是否校验可用性 */
    private boolean validateCheck = false;

    /** 检查数据库连接的线程的运行时间间隔，单位为毫秒 */
    private long checkIntervalMS = 0L;

    /** 驱动类名 */
    private String driverClassName = null;

    /** 默认schema */
    private String schema = null;

    /** 用户名 */
    private String username = null;

    /** 密码 */
    private String password = null;

    /** 目标数据库的连接字符串 */
    private String url = null;

    /** 是否快速启动，快速启动时，不初始化最小连接 */
    private boolean fastStartUp;

    public String getDbDivName() {
        return dbDivName;
    }

    public void setDbDivName(String dbDivName) {
        this.dbDivName = dbDivName;
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        if(minSize <= 0) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性minSize的值不正确" + minSize);
        }
        if(this.maxSize > 0 && minSize > maxSize) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性minSize的值不正确" + minSize);
        }
        this.minSize = minSize;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        if(maxSize <= 0) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性maxSize的值不正确" + maxSize);
        }
        if(this.minSize > 0 && maxSize < this.minSize) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性maxSize的值不正确" + maxSize);
        }
        this.maxSize = maxSize;
    }

    public int getIdleThreshold() {
        return idleThreshold;
    }

    public void setIdleThreshold(int idleThreshold) {
        if(idleThreshold <= 0) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性idleThreshold的值不正确" + idleThreshold);
        }
        this.idleThreshold = idleThreshold;
    }

    public int getAdditionalCount() {
        return additionalCount;
    }

    public void setAdditionalCount(int additionalCount) {
        if(additionalCount <= 0) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性additionalCount的值不正确" + additionalCount);
        }
        this.additionalCount = additionalCount;
    }

    public long getRecycleTimeMS() {
        return recycleTimeMS;
    }

    public void setRecycleTimeMS(long recycleTimeMS) {
        if(recycleTimeMS <= 0) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性recycleTimeMS的值不正确" + recycleTimeMS);
        }
        this.recycleTimeMS = recycleTimeMS;
    }

    public int getRecycleCount() {
        return recycleCount;
    }

    public void setRecycleCount(int recycleCount) {
        this.recycleCount = recycleCount;
    }

    public boolean isAutoCommit() {
        return autoCommit;
    }

    public void setAutoCommit(String autoCommit) {
        if(YES.equals(autoCommit)) {
            this.autoCommit = true;
        } else if(NO.equals(autoCommit)) {
            this.autoCommit = false;
        } else {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性autoCommit的值不正确" + autoCommit);
        }
    }

    public long getTransactionTimeoutMS() {
        return transactionTimeoutMS;
    }

    public void setTransactionTimeoutMS(long transactionTimeoutMS) {
        this.transactionTimeoutMS = transactionTimeoutMS;
    }

    public int getConnectionTimeoutSeconds() {
        return connectionTimeoutSeconds;
    }

    public void setConnectionTimeoutSeconds(int connectionTimeoutSeconds) {
        if(recycleTimeMS <= 0) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性connectionTimeoutSeconds的值不正确" + connectionTimeoutSeconds);
        }
        this.connectionTimeoutSeconds = connectionTimeoutSeconds;
    }

    public long getRetrieveTimeoutMS() {
        return retrieveTimeoutMS;
    }

    public void setRetrieveTimeoutMS(long retrieveTimeoutMS) {
        if(recycleTimeMS <= 0) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性retrieveTimeoutMS的值不正确" + retrieveTimeoutMS);
        }
        this.retrieveTimeoutMS = retrieveTimeoutMS;
    }

    public boolean isValidateCheck() {
        return validateCheck;
    }

    public void setValidateCheck(String validateCheck) {
        if(YES.equals(validateCheck)) {
            this.validateCheck = true;
        } else if(NO.equals(validateCheck)) {
            this.validateCheck = false;
        } else {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性validateCheck的值不正确" + validateCheck);
        }
    }

    public long getCheckIntervalMS() {
        return checkIntervalMS;
    }

    public void setCheckIntervalMS(long checkIntervalMS) {
        if(recycleTimeMS <= 0) {
            throw new RuntimeException("分库名称为" + dbDivName + "的数据库连接池中，属性checkIntervalMS的值不正确" + checkIntervalMS);
        }
        this.checkIntervalMS = checkIntervalMS;
    }


    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName.trim();
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username.trim();
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url.trim();
    }

    public boolean isFastStartUp() {
        return fastStartUp;
    }

    public void setFastStartUp(boolean fastStartUp) {
        this.fastStartUp = fastStartUp;
    }
}
