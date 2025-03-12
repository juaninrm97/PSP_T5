package es.studium.Tema5;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.net.ssl.SSLSocket;

public class HiloServidorSSL extends Thread {
	DataInputStream fentrada;
	SSLSocket socket;
	boolean fin = false;

	public HiloServidorSSL(SSLSocket socket) {
		this.socket = socket;
		try {
			fentrada = new DataInputStream(socket.getInputStream());
		} catch (IOException e) {
			System.out.println("Error de E/S");
			e.printStackTrace();
		}
	}

	public void run() {
		ServidorChatSSL.mensaje.setText("Número de conexiones actuales: " + ServidorChatSSL.ACTUALES);
		String texto = ServidorChatSSL.textarea.getText();
		EnviarMensajes(texto);

		while (!fin) {
			String cadena = "";
			try {
				cadena = fentrada.readUTF();
				if (cadena.trim().equals("*")) {
					ServidorChatSSL.ACTUALES--;
					ServidorChatSSL.mensaje.setText("Número de conexiones actuales: " + ServidorChatSSL.ACTUALES);
					fin = true;
				} else {
					ServidorChatSSL.textarea.append(cadena + "\n");
					texto = ServidorChatSSL.textarea.getText();
					EnviarMensajes(texto);
				}
			} catch (Exception ex) {
				ex.printStackTrace();
				fin = true;
			}
		}
	}

	private void EnviarMensajes(String texto) {
		for (int i = 0; i < ServidorChatSSL.CONEXIONES; i++) {
			SSLSocket socket = ServidorChatSSL.tabla[i];
			try {
				DataOutputStream fsalida = new DataOutputStream(socket.getOutputStream());
				fsalida.writeUTF(texto);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
