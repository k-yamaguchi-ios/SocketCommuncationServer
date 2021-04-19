package socket_practice;

public class QueryExecutor {
    
    public int initializeExecutor( String address, String username, String password ) {
        return 0;
    }

    public int finalizeExecutor() {
        return 0;
    }

    public int exeQuery( String query, QueryExeCallback callback ) {
        callback.onReceive("query result");
        return 0;
    }
}
