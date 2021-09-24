package project;

import java.net.*;
import java.util.*;
import java.io.*;
import javax.net.ssl.*;

class POPClient extends Thread{
    Socket client;
    PrintWriter c_pw;

    String serverResponse(Socket s) throws IOException{
        String str="";
        char c;

        // terminator --> <CRLF>.<CRLF>
        while((c=(char)s.getInputStream().read())!='\n'){
            str+=c;
            //System.out.println(str);
        }
        str+='\n';
        return str;
    }

    String msgBody(Socket s) throws IOException{
        String str="";
        char c;
        char prev='\n';
        char prev_prev='\n';
        boolean flg=true;
        while(flg){
            c=(char)s.getInputStream().read();
            if(prev_prev=='\r' && prev=='\n' && c=='.'){
                flg=false;
            }
            str+=c;
            prev_prev=prev;
            prev=c;
        }

        return str;
    }

    SSLSocket connect() throws IOException{
        SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket sslSocket = (SSLSocket) factory.createSocket("pop.gmail.com",995);
        return sslSocket;
    }

    boolean authentication(SSLSocket sslSocket) throws IOException{
        PrintWriter pw = new PrintWriter(sslSocket.getOutputStream(),true);

        System.out.print(this.serverResponse(sslSocket));
        pw.println("user use.for.testing.not.safe@gmail.com");
        System.out.println(this.serverResponse(sslSocket));
        pw.println("pass notsafe.2021");
        System.out.println(this.serverResponse(sslSocket));

        return false;
    }

    boolean listEmails(SSLSocket sslSocket) throws IOException{
        PrintWriter pw = new PrintWriter(sslSocket.getOutputStream());

        pw.println("list ");
        //System.out.println(this.serverResponse(sslSocket));
        //System.out.println(this.msgBody(sslSocket));

        return false;
    }

    boolean quit(SSLSocket sslSocket) throws IOException{
        PrintWriter pw = new PrintWriter(sslSocket.getOutputStream(),true);

        pw.println("quit");
        System.out.println(this.serverResponse(sslSocket));

        return false;
    }

    public void start_session() throws IOException{
        SSLSocket sslSocket;

        sslSocket = this.connect();
        this.authentication(sslSocket);
        /*
        System.out.print(this.serverResponse(sslSocket));
        pw.println("user use.for.testing.not.safe@gmail.com");
        System.out.println(this.serverResponse(sslSocket));
        pw.println("pass notsafe.2021");
        System.out.println(this.serverResponse(sslSocket));
        */

        this.listEmails(sslSocket);

        /*
        pw.println("list ");
        System.out.println(this.serverResponse(sslSocket));
        System.out.println(this.msgBody(sslSocket));
        */
        /*
        pw.println("retr 1");
        System.out.println(this.serverResponse(sslSocket));
        System.out.println(this.msgBody(sslSocket));

        pw.println("dele 1");
        System.out.println(this.serverResponse(sslSocket));
        System.out.println(this.serverResponse(sslSocket));
	    */

        this.quit(sslSocket);

        /*
        pw.println("quit");
        System.out.println(this.serverResponse(sslSocket));
        */
    }

    public void run(){
        try{
            this.start_session();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}
