package es.studium.Tema5;

import javax.net.ssl.*;
import java.io.*;
import javax.swing.*;

public class ClienteChatSSL extends JFrame {
    private static final long serialVersionUID = 1L;
    static final String HOST = "127.0.0.1";
    static final int PUERTO = 44444;
    SSLSocket socket;
    DataInputStream fentrada;
    DataOutputStream fsalida;
    static JTextField mensaje = new JTextField();
    static JTextArea textarea;
    JButton boton = new JButton("Enviar");
    JButton desconectar = new JButton("Salir");
    boolean repetir = true;
    String nombre;

    public ClienteChatSSL(SSLSocket socket, String nombre) {
        super("Conexión segura del cliente chat: " + nombre);
        this.socket = socket;
        this.nombre = nombre;

        // Configurar ventana
        setLayout(null);
        mensaje.setBounds(10, 10, 400, 30);
        add(mensaje);
        textarea = new JTextArea();
        JScrollPane scrollpane = new JScrollPane(textarea);
        scrollpane.setBounds(10, 50, 400, 300);
        add(scrollpane);
        boton.setBounds(420, 10, 100, 30);
        add(boton);
        desconectar.setBounds(420, 50, 100, 30);
        add(desconectar);
        textarea.setEditable(false);
        boton.addActionListener(e -> enviarMensaje());
        desconectar.addActionListener(e -> salir());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Configurar streams
        try {
            fentrada = new DataInputStream(socket.getInputStream());
            fsalida = new DataOutputStream(socket.getOutputStream());
            fsalida.writeUTF("SERVIDOR> Entra en el chat... " + nombre);
            fsalida.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        // Configuración de seguridad SSL
        System.setProperty("javax.net.ssl.trustStore", "clienteTrustStore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "studiumd2024");
        System.setProperty("javax.net.debug", "all"); // Depuración SSL

        // Crear socket SSL
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(HOST, PUERTO);

        // 🔹 FORZAR PROTOCOLO TLS 1.2
        socket.setEnabledProtocols(new String[]{"TLSv1.2"});

        // 🔹 INICIAR HANDSHAKE SSL
        socket.startHandshake();

        String nombre = JOptionPane.showInputDialog("Introduce tu nombre o nick:");
        if (nombre == null || nombre.trim().isEmpty()) {
            System.out.println("El nombre no puede estar vacío.");
            System.exit(0);
        }

        ClienteChatSSL cliente = new ClienteChatSSL(socket, nombre);
        cliente.setBounds(0, 0, 540, 400);
        cliente.setVisible(true);
        cliente.ejecutar();
    }

    public void enviarMensaje() {
        try {
            String texto = nombre + "> " + mensaje.getText();
            mensaje.setText("");
            fsalida.writeUTF(texto);
            fsalida.flush();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void salir() {
        try {
            fsalida.writeUTF("SERVIDOR> Abandona el chat... " + nombre);
            fsalida.writeUTF("*"); // Señal para cerrar la conexión
            fsalida.flush();
            repetir = false;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void ejecutar() {
        while (repetir) {
            try {
                String texto = fentrada.readUTF();
                textarea.setText(texto);
            } catch (IOException ex) {
                System.out.println("Conexión cerrada.");
                repetir = false;
            }
        }
        try {
            socket.close();
            System.exit(0);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
