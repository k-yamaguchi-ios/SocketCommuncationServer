package socket_practice;

public class QueryExecutor {
    private static final int FAILURE = -1;
    BaseDBManager dbManager;
    
    public int initializeExecutor( String address, String username, String password ) {
        int result;
        dbManager = new OracleManager();
        result = dbManager.connect(address, username, password);
        return result;
    }

    public int finalizeExecutor() {
        int result = FAILURE;
        if ( dbManager != null ) {
            dbManager.disconnect();
            dbManager = null;
        }
        return result;
    }

    public int exeQuery( String query, QueryExeCallback callback ) {
        ExeThread thread = new ExeThread(query, callback);
        thread.run();
        return 0;
    }

    private class ExeThread extends Thread {
        private static final String NOT_INITALIZED = "DB is not connected";
        private String mQuery;
        private QueryExeCallback mCallback;

        public ExeThread( String query, QueryExeCallback callback ) {
            mQuery = query;
            mCallback = callback;
        }

        @Override
        public void run(){
            String result = null;
            if ( null != dbManager ){
                result = dbManager.exeQuery( mQuery );
            } else {
                result = NOT_INITALIZED;
            }
            mCallback.onReceive( result );
        }
    }
}
