package telegram_bot;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Main {

	public static void main(String[] args) {

		// Definindo quais tipos de logs devem ser habilitados:
		boolean comentario = true;
		boolean mensagemEnviada = true;
		boolean mensagemRecebida = true;
		boolean erro = true;
		Console.habilitarExibicao(comentario, mensagemEnviada, mensagemRecebida, erro);

		try {
			// Configurando acesso ao arquivo de configura��es.
			// Para rodar esta aplica��o � necess�rio criar um arquivo
			// "config.properties" baseado no arquivo "config-sample.properties"
			// e nele preencher as vari�veis solicitadas
			Properties prop = new Properties();
			FileInputStream ip = new FileInputStream("config.properties");
			prop.load(ip);

			// Recuperando as chaves de acesso
			String tokenTelegramBot = prop.getProperty("token_telegram_bot");
			String apiKeyMovies = prop.getProperty("api_key_movies");

			// Caso alguma das vari�veis n�o esteja preenchida, lan�a uma exce��o informando
			// o problema
			if (tokenTelegramBot.isEmpty() || apiKeyMovies.isEmpty()) {
				throw new Exception("� necess�rio preencher as vari�veis de configura��o no arquivo config.properties");
			}

			// Criando um objeto do tipo BugBot que gerenciar� todas as intera��es com o bot
			BugBot bugBot = new BugBot(tokenTelegramBot, apiKeyMovies);

			// Loop infinito para que as mensagens enviadas pelo usu�rio sejam computadas e
			// respondidas logo em seguida
			while (true) {
				bugBot.atualizar();
			}
		} catch (IOException ex) {
			// Exibindo informativo de como configurar o projeto assim que tiver clonado do github
			Console.printarComentario("Ops, ocorreu um erro!!", true);
			Console.printarComentario("Para rodar o bot, siga os seguintes passos:", true);
			Console.printarComentario(
					"1 - Duplique o arquivo config-sample.properties e renomeie para config.properties", true);
			Console.printarComentario(
					"2 - Preencha as chaves de acesso no config.properties de acordo com o bot criado no BotFather e com a API de filmes",
					true);
		} catch (Exception ex) {
			Console.printarComentario("Ocorreu um erro: " + ex.getMessage(), true);
		}

	}
}
