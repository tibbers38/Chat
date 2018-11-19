package test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.lang.String;

class Name extends UDPClient {
	Scanner sc;
	String nickName = null;
	
    public void getNickName () {
    	System.out.println("Enter your nickname: ");
    	sc = new Scanner(System.in);
    	this.nickName = sc.nextLine();
    }
}

class MulticastSender extends Thread {
	// send data
    public static final String GROUP_ADDRESS = "224.0.0.1";
    public static final int PORT = 8888;
 
    public void run() {
    	DatagramSocket socket = null;
    	Name n = null;
    	
        n = new Name();
	    n.getNickName();
	    
        try {
            // Get the address that we are going to connect to.
            InetAddress groupAddress = InetAddress.getByName(GROUP_ADDRESS);
            // Create a new Multicast socket
            socket = new DatagramSocket();
            while (true) {
            	String text = inputMessage();
	            byte[] data = createData(n, text);
                sendData(socket, groupAddress, data);
            }
        } 
        catch (IOException ex) {
            ex.printStackTrace();
        } 
        finally {
            if (socket != null) {
                socket.close();
            }
            n.sc.close();
        }
    }

	private byte[] createData(Name n, String text) {
		String newString = text.concat(":"+n.nickName);
		byte[] data = newString.getBytes(); // Đổi chuỗi ra mảng bytes
		return data;
	}

	private String inputMessage() throws IOException {
		System.out.println("Enter your message: ");
		InputStreamReader isr = new InputStreamReader(System.in); // Nhập
		BufferedReader br = new BufferedReader(isr); // một chuỗi
		String text = br.readLine(); // từ bàn phím
		return text;
	}

	private void sendData(DatagramSocket socket, InetAddress groupAddress, byte[] data) throws IOException {
		DatagramPacket outPacket;
		outPacket = new DatagramPacket(data, data.length, groupAddress, PORT);
		socket.send(outPacket);
	}
}

public class UDPClient {
	// receive data
    public static final byte[] BUFFER = new byte[4096];

	public static void main(String[] args) throws InterruptedException { 
		MulticastSocket socket;
		DatagramPacket inPacket;
		String content;
        String nickName;
		
    	startSenderThread();
		try {
            socket = joinChatGroup();
            while (true) {
                // Receive the information and print it.
                inPacket = new DatagramPacket(BUFFER, BUFFER.length);
                socket.receive(inPacket);
                String data = new String(BUFFER, 0, inPacket.getLength());
                
                // xu li data nhan duoc
                if (data.lastIndexOf(':', data.length()) == data.length()-1) {
	                content = data.replace(':', '\0');
	                nickName = "no";
                }
                else {
                	String[] splitedData = data.split(":");
                	content = splitedData[0];
                	nickName = splitedData[1];
                }
                
                if (nickName.equals("no")) {
                	System.out.println("From someone with IP " + inPacket.getAddress() + ": " + content);
                }
                else System.out.println("From " + nickName + ": " + content);
            }
        } catch (IOException ex1) {
            ex1.printStackTrace();
        }
	}

	private static void startSenderThread() {
		MulticastSender hello = new MulticastSender();
		hello.start();
	}

	private static MulticastSocket joinChatGroup() throws UnknownHostException, IOException {
		MulticastSocket socket;
		// Get the address that we are going to connect to.
		InetAddress address = InetAddress.getByName(MulticastSender.GROUP_ADDRESS);
		// Create a new Multicast socket
		socket = new MulticastSocket(MulticastSender.PORT);
		// Join the Multicast group
		socket.joinGroup(address);
		return socket;
	}
}