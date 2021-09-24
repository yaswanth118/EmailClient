package project;

import javax.net.ssl.*;
import java.util.Base64;
import java.net.*;
import java.util.Scanner;
import java.io.*;

class SMTPClient extends Thread {
    //TODO : redirecting output to client and getting input from client instead of System.in is pending
    //TODO : refactoring scanners and printwriter out of most of the methods
    //TODO : If possible add as many MIME types to hashmap and prompt
    Socket client;
    PrintWriter c_pw;

    String serverResponse(Socket s) throws IOException{
        String str="";
        char c;
        try{
            while((c=(char)s.getInputStream().read())!='\n'){
                str+=c;
                //System.out.println(s.hashCode()+"  "+str + " "+c);
            }
        }
        catch(SocketException se){
            se.printStackTrace();
        }
        return str;
    }

    boolean handleError(String str){
        //fix error conditions
        boolean error = (str.charAt(0)=='5');
        if(error){
            System.out.println("Error in conversation with smtp.gmail.com");
            System.out.println("Reconnecting...\n");
            return true;
        }
        return false;
    }

    Socket connect() throws IOException{
        InetAddress i = InetAddress.getByName("smtp.gmail.com");
        Socket s = new Socket(i,587);
        return s;
    }

    boolean greetServer(Socket s) throws IOException{
        PrintWriter pw = new PrintWriter(s.getOutputStream(),true);
        String str = this.serverResponse(s);
        if(this.handleError(str)){
            return true;
        }
        System.out.println(str);

        pw.println("helo smtp.gmail.com");
        str = this.serverResponse(s);
        if(this.handleError(str)){
            return true;
        }
        System.out.println(str);
        return false;
    }

    boolean startTLS(Socket s) throws IOException{
        PrintWriter pw = new PrintWriter(s.getOutputStream(),true);
        String str;
        pw.println("STARTTLS");
        str = this.serverResponse(s);
        if(this.handleError(str)){
            return true;
        }
        return false;
    }

    boolean authentication(SSLSocket sslSocket) throws IOException{
        PrintWriter pw = new PrintWriter(sslSocket.getOutputStream(),true);
        String str;

        c_pw.println("+ Before Trying to login check whether your account gave access to low level apps or not");
        pw.println("auth login");
        str = this.serverResponse(sslSocket);
        if(this.handleError(str)){
            return true;
        }
        System.out.println(str);

        String c_str;
        c_pw.println("- Enter your Email-id : ");
        c_str = this.serverResponse(client);
        System.out.println(c_str);
        //str="use.for.testing.not.safe@gmail.com";
        c_str = new String(Base64.getEncoder().encode(c_str.getBytes()));

        pw.println(c_str);
        str = this.serverResponse(sslSocket);
        if(this.handleError(str)){
            return true;
        }
        System.out.println(str);

        //System.out.print("Enter Associated Password : ");
        c_pw.println("- Enter Associated Password : ");
        //uncomment it
        //str = sc.nextLine();
        c_str = this.serverResponse(client);
        //str="notsafe.2021";
        c_str=new String(Base64.getEncoder().encode(c_str.getBytes()));

        pw.println(c_str);
        str = this.serverResponse(sslSocket);
        if(this.handleError(str)){
            return true;
        }
        System.out.println(str);

        return false;
    }

    boolean getMailIDs(SSLSocket sslSocket) throws IOException{
        PrintWriter pw=new PrintWriter(sslSocket.getOutputStream(),true);
        Scanner sc = new Scanner(client.getInputStream());
        String str,c_str;

        //System.out.print("MAIL FROM :");
        c_pw.println("- MAIL FROM :");
        //System.out.println("what ?");
        c_str = this.serverResponse(client);
        c_str = c_str.replace('\r', ' ');
        //System.out.println(c_str.length());
        //error is here with \r character
        System.out.println("MAIL FROM:<"+c_str+">");
        pw.println("MAIL FROM:<"+c_str+">");
        str = this.serverResponse(sslSocket);
        if(this.handleError(str)){
            return true;
        }
        System.out.println(str);

        System.out.print("RCPT TO :");
        c_pw.println("- RCPT TO :");
        c_str = this.serverResponse(client);
        c_str = c_str.replace('\r', ' ');
        c_str = c_str.trim();
        System.out.println("RCPT TO:<"+c_str+">");
        pw.println("RCPT TO:<"+c_str+">");
        str = this.serverResponse(sslSocket);
        if(this.handleError(str)){
            return true;
        }
        System.out.println(str);

        return false;
    }

    boolean msgBody(SSLSocket sslSocket) throws IOException{
        PrintWriter pw = new PrintWriter(sslSocket.getOutputStream(),true);
        String str;

        pw.println("data");
        str = this.serverResponse(sslSocket);
        if(this.handleError(str)){
            return true;
        }
        System.out.println(str);

        this.getMsgBody(sslSocket);
        str = this.serverResponse(sslSocket);
        if(this.handleError(str)){
            return true;
        }
        System.out.println(str);

        return false;
    }

    void getMsgBody(Socket s) throws IOException{
        //System.out.println("Please enter '+'  in new line to end the message");
        c_pw.println("+ Please enter '+'  in new line to end the message");

        String str="",c_str;
        PrintWriter pw = new PrintWriter(s.getOutputStream(),true);
        String header="";

        c_pw.println("- Subject :");
        c_str = this.serverResponse(client);
        header+="Subject : "+c_str+"\n";
        c_pw.println("- From :");
        c_str = this.serverResponse(client);
        header+="From : "+c_str+"\n";
        c_pw.println("- To :");
        c_str = this.serverResponse(client);
        header+="To : "+c_str+"\n";
        header+="MIME-Version : 1.0\r\n";
        header+="Content-Type : multipart/mixed; boundary=\"boundary\"\r\n";
        pw.println(header);
        System.out.println(header);

        c_pw.println("*");

        String temp;
        c_str="";
        String terminator="<o>_<o>";
        while(!(temp = this.serverResponse(client)).trim().equals(terminator)){
            System.out.println(temp);
            c_str+=temp+"\r\n";
        }
        pw.println(c_str);
        pw.println("\r\n.\r\n");
    }

    boolean quit(SSLSocket sslSocket) throws IOException{
        PrintWriter pw = new PrintWriter(sslSocket.getOutputStream(),true);

        pw.println("quit");
        c_pw.println("quit");
        System.out.println(this.serverResponse(sslSocket));

        return false;
    }

    void start_session() throws IOException{
        try{
            SSLSocketFactory factory=(SSLSocketFactory) SSLSocketFactory.getDefault();
            Socket s;
            SSLSocket sslSocket;
            Scanner sc = new Scanner(client.getInputStream());
            boolean error_flg=true;

            start:{
                do{
                    s= this.connect();
                    System.out.println("HELLO");
                    System.out.println(s.getInetAddress()+" "+s.getPort());
                    error_flg = this.greetServer(s);
                    error_flg = this.startTLS(s);
                    sslSocket = (SSLSocket)(factory.createSocket(s, "smtp.gmail.com", 587, false));
                    error_flg = this.authentication(sslSocket);
                }while(error_flg);

                char c;
                do{
                    this.getMailIDs(sslSocket);
                    this.msgBody(sslSocket);
                    c_pw.println("- Do you want to send another email? [Y/N]");
                    c = this.serverResponse(client).charAt(0);
                }while(c!='N');
                this.quit(sslSocket);
            }

        }
        catch(Exception e){
            client.close();
            e.printStackTrace();
        }

    }

    public void run(){
        try{
            c_pw.println("- hello client");
            this.serverResponse(client);
            this.start_session();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

}