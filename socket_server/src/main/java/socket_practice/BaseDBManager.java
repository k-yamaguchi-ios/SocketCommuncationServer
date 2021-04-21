package socket_practice;

public abstract class BaseDBManager {
    public abstract int connect( String address, String username, String password );
    public abstract int disconnect();
    public abstract String exeQuery( String query );
}
