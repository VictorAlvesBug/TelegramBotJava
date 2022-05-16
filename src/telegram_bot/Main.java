package telegram_bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

	/*
	 TODO:
	 	Corrigir regex de saudação
	 	Corrigir bug das multiplas respostas.
	 	Ordenar comandos ao exibir no chat.
	 	Descobrir como limpar o cache da aplicação.
	 	
	 */
	
	public static void main(String[] args) throws IOException {
		// Configurando acesso ao arquivo de variáveis globais
		Properties prop = new Properties();
		FileInputStream ip = new FileInputStream("config.properties");
		prop.load(ip);
		
		// Criando um objeto do tipo BugBot
		BugBot bugBot = new BugBot(prop.getProperty("token"));

		// Loop infinito pode ser alterado por algum timer de intervalo curto.
		while (true) {
			bugBot.atualizar();
		}
	}
}
