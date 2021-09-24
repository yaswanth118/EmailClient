package project;

import java.net.*;
import java.io.*;
class Server {

    static String serverResponse(Socket s) throws IOException{
        String str="";
        char c;
        while((c=(char)s.getInputStream().read())!='\n'){
            str+=c;
        }
        return str;
    }
    public static void main(String[] args) throws UnknownHostException,IOException{
        int backlog = 5;
        InetAddress i = InetAddress.getByName("127.0.0.1");
        //192.168.43.133
        System.out.println("Server started running at IP:"+i.toString().replace('/',' '));
        ServerSocket server = new ServerSocket(2345, backlog, i);

        while(true){
            Socket client = server.accept();
            PrintWriter pw = new PrintWriter(client.getOutputStream(),true);
            int choice = 0;
            pw.println("Enter : 1 to access SMTP (or) 2 to access POP");
            choice = Server.serverResponse(client).charAt(0)-'0';

            if(choice==1){
                SMTPClient smtp = new SMTPClient();
                smtp.client = client;
                smtp.c_pw = new PrintWriter(client.getOutputStream(),true);
                System.out.println("smtp thread started");
                smtp.start();
                System.out.println("smtp thread returned");
            }
            else{
                POPClient pop = new POPClient();
                pop.client = client;
                pop.c_pw = new PrintWriter(client.getOutputStream(),true);
                System.out.println("pop thread started");
                pop.start();
                System.out.println("pop thread returned");
            }
        }
    }
}
