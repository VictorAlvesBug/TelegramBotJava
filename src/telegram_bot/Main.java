package telegram_bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

	public static void main(String[] args) {

		// Definindo quais tipos de logs devem ser habilitados:
		boolean comentario = false;
		boolean mensagemEnviada = false;
		boolean mensagemRecebida = false;
		boolean erro = false;
		Console.habilitarExibicao(comentario, mensagemEnviada, mensagemRecebida, erro);

		try {
			// Configurando acesso ao arquivo de variáveis globais.
			// Para rodar esta aplicação é necessário criar um arquivo
			// "config.properties" baseado no arquivo "config-sample.properties"
			// e nele preencher as variáveis solicitadas
			Properties prop = new Properties();
			FileInputStream ip = new FileInputStream("config.properties");
			prop.load(ip);

			// Recuperando as chaves de acesso
			String tokenTelegramBot = prop.getProperty("token_telegram_bot");
			String apiKeyMovies = prop.getProperty("api_key_movies");

			// Criando um objeto do tipo BugBot
			BugBot bugBot = new BugBot(tokenTelegramBot, apiKeyMovies);

			// Loop infinito pode ser alterado por algum timer de intervalo curto.
			while (true) {
				bugBot.atualizar();
			}
		} catch (IOException ex) {
			Console.printarComentario("Ocorreu um erro ao acessar o arquivo config.properties", true);
		} catch (Exception ex) {
			Console.printarComentario("Ocorreu um erro: " + ex.getMessage(), true);
		}

	}
}
