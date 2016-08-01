package com.ingoguilherme.cadastrolivros;

import android.util.Log;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao {
    private static Connection con;
    private static Statement stm;
    private static boolean autoCommitConexao;

    private static String host = "192.168.1.8:3306";
    
    public static boolean conectar(String db, String usr, String pwd){
    	autoCommitConexao = true;

        try{
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://"+host+"/" + db, usr, pwd);
            stm = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            Log.d("CONEXAO","Conectado");
            return true;
        }
        catch(SQLException ex){
            Log.d("CONEXAO","SQLEXception "+ ex.getMessage());
            Log.d("CONEXAO","SQLState "+ ex.getSQLState());
            System.exit(0);
        }
        catch(Exception e){
            Log.d("CONEXAO","Não foi possível conectar ao banco \n"+e.getMessage());
            System.exit(0);
        }

        return false;
    }
    
    public static void conectar(String db, String usr, String pwd, boolean autoCommit){
    	autoCommitConexao = autoCommit;
    	
        try{
            Class.forName("com.mysql.jdbc.Driver");
            con = DriverManager.getConnection("jdbc:mysql://"+host+"/" + db, usr, pwd);
            con.setAutoCommit(autoCommitConexao);
            stm = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            Log.d("CONEXAO","Conectado");
        }
        catch(SQLException ex){
            Log.d("CONEXAO","SQLEXception "+ ex.getMessage());
            Log.d("CONEXAO","SQLState "+ ex.getSQLState());
            System.exit(0);
        }
        catch(Exception e){
            Log.d("CONEXAO","Não foi possível conectar ao banco \n"+e.getMessage());
            System.exit(0);
        }
    }

    public static void commit() throws SQLException{
    	if(!autoCommitConexao){
    		con.commit();
    	}
    }
    
    public static void rollback(Savepoint sp) throws SQLException{
    	con.rollback(sp);
    }
    
    public static void rollback() throws SQLException{
    	con.rollback();
    }
    
    public static Savepoint setSavePoint(String spName) throws SQLException{
    	return con.setSavepoint(spName);
    }
    
    public static Savepoint setSavePoint() throws SQLException{
    	return con.setSavepoint();
    }
    
    public static boolean isNull(){
    	return con == null;
    }
    
    public static PreparedStatement prepareStatement(String query) throws SQLException{
    	PreparedStatement ps = con.prepareStatement(query);
    	return ps;
    }
    
    public static ResultSet executeQuery(String query) throws SQLException{
    	return stm.executeQuery(query);
    }
    
    public static void fechar(){
        try {
        	stm.close();
        	con.close();
            Log.d("CONEXAO","Conexão fechada");
        } catch (SQLException e) {
            Log.d("CONEXAO",e.getMessage());
        }
    }
}