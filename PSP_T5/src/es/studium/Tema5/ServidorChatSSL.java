package es.studium.Tema5;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ssl.*;
import javax.swing.*;

public class ServidorChatSSL extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	static SSLServerSocket servidor;
	static final int PUERTO = 44444;
	static int CONEXIONES = 0;
	static int ACTUALES = 0;
	static int MAXIMO = 15;
	static JTextField mensaje = new JTextField("");
	static JTextField mensaje2 = new JTextField("");
	private JScrollPane scrollpane1;
	static JTextArea textarea;
	JButton salir = new JButton("Salir");
	static SSLSocket[] tabla = new SSLSocket[MAXIMO];

	public ServidorChatSSL() {
		super(" VENTANA DEL SERVIDOR DE CHAT ");
		setLayout(null);
		mensaje.setBounds(10, 10, 400, 30);
		add(mensaje);
		mensaje.setEditable(false);
		mensaje2.setBounds(10, 348, 400, 30);
		add(mensaje2);
		mensaje2.setEditable(false);
		textarea = new JTextArea();
		scrollpane1 = new JScrollPane(textarea);
		scrollpane1.setBounds(10, 50, 400, 300);
		add(scrollpane1);
		salir.setBounds(420, 10, 100, 30);
		add(salir);
		textarea.setEditable(false);
		salir.addActionListener(this);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public static void main(String args[]) throws Exception {
		// Cargar el keystore con el certificado
		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream("juaninKeystore.jks"), "studiumd2024".toCharArray());

		// Configurar el gestor de claves
		KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
		kmf.init(keyStore, "studiumd2024".toCharArray());

		// Configurar contexto SSL
		SSLContext sslContext = SSLContext.getInstance("TLS");
		sslContext.init(kmf.getKeyManagers(), null, null);

		// Crear el servidor SSL
		SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
		servidor = (SSLServerSocket) factory.createServerSocket(PUERTO);
		System.out.println("Servidor SSL iniciado en el puerto " + PUERTO);

		ServidorChatSSL pantalla = new ServidorChatSSL();
		pantalla.setBounds(0, 0, 540, 450);
		pantalla.setVisible(true);
		mensaje.setText("Número de conexiones actuales: " + 0);

		while (CONEXIONES < MAXIMO) {
			SSLSocket socket;
			try {
				socket = (SSLSocket) servidor.accept();
			} catch (IOException ex) {
				break;
			}
			tabla[CONEXIONES] = socket;
			CONEXIONES++;
			ACTUALES++;
			HiloServidorSSL hilo = new HiloServidorSSL(socket);
			hilo.start();
		}
		if (!servidor.isClosed()) {
			try {
				mensaje2.setForeground(Color.red);
				mensaje2.setText("Máximo Nº de conexiones establecidas: " + CONEXIONES);
				servidor.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		} else {
			System.out.println("Servidor finalizado...");
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == salir) {
			try {
				servidor.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			System.exit(0);
		}
	}
}
