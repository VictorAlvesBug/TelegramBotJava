package telegram_bot;

import java.util.ArrayList;
import java.util.List;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendMessage;

public class BugBot {

	// Criando objeto "bot" que será responsável por enviar e receber as mensagens
	private TelegramBot bot;

	// Armazena o id da última mensagem lida, para fazer offset ao ler as mensagens
	// pendentes
	private int idMensagemJaLida;

	// Construtor recebe as chaves de acesso via parâmetro
	public BugBot(String tokenTelegramBot, String apiKeyMovies) {

		// Caso não seja informado o token, lança exceção
		if(tokenTelegramBot == null || tokenTelegramBot.isEmpty())
		{
			return;
		}
		
		// Adicionando informações de acesso ao bot
		bot = new TelegramBot(tokenTelegramBot);

		// Inicializando o id da última mensagem lida como zero, para que ao iniciar a
		// conversa todas as mensagens sejam consideradas como não-lidas
		idMensagemJaLida = 0;

		// Define quais são os comandos disponíveis no bot
		GerenciadorComandosBot.getInstancia(apiKeyMovies);
	}

	// Método executado em loop, para verificar se há alguma mensagem recebida do usuário
	public void atualizar() {
		
		// Retorna apenas as mensagem recebidas por último e ainda não analizadas
		List<Update> listaMensagensRecebidas = bot.execute(new GetUpdates().limit(100).offset(idMensagemJaLida))
				.updates();

		// Caso não tenha nenhuma mensagem pendente, aborta a execução do método
		if (listaMensagensRecebidas == null) {
			return;
		}

		// Analiza cada mensagem recebida
		for (Update mensagemRecebida : listaMensagensRecebidas) {
			
			// Incrementa id da última mensagem lida, para que ela seja desconsiderada 
			// na próxima iteração.
			idMensagemJaLida = mensagemRecebida.updateId() + 1;

			// Interpreta mensagem recebida e define uma resposta adequada.
			tratarMensagemRecebida(mensagemRecebida);
		}
	}

	private void tratarMensagemRecebida(Update mensagem) {
		// Caso a mensagem não esteja disponível, aborta a execução do método
		if (mensagem == null || mensagem.message() == null || mensagem.message().chat() == null
				|| mensagem.message().chat().id() == null || mensagem.message().text() == null) {
			return;
		}

		// Armazena o id do chat que enviou as mensagens para o bot, para 
		// futuramente responder ao chat correto
		Long chatId = mensagem.message().chat().id();
		
		// Armazena apenas o comando recebido
		String strComando = mensagem.message().text().trim();

		// Exibe no console o comando recebido
		Console.printarMensagemRecebida(strComando);

		// Envia "Digitando..." antes de enviar a resposta
		enviarAvisoDigitando(chatId);

		// Envia a resposta de acordo com o comando recebido
		responderComando(chatId, strComando);
	}

	private void enviarAvisoDigitando(Long chatId) {
		
		// Envia "Digitando..."
		String digitando = ChatAction.typing.name();
		boolean avisoDigitandoFoiEnviado = bot.execute(new SendChatAction(chatId, digitando)).isOk();

		// Exibe no console se "Digitando..." foi enviado com sucesso ou não
		printarStatusMensagemEnviada(avisoDigitandoFoiEnviado, digitando);
	}

	private void responderComando(Long chatId, String comando) {
		// Retorna a resposta que será enviada na conversa
		String listaRespostas = retornarRespostas(comando);
		
		// Envia resposta
		boolean enviou = bot.execute(new SendMessage(chatId, listaRespostas)).isOk();

		// Exibe no console se a resposta foi enviada com sucesso ou não
		printarStatusMensagemEnviada(enviou, listaRespostas);
	}

	private String retornarRespostas(String strComandos) {
		
		// Inicializa um buffer de strings, para possibilitar o acúmulo de respostas em
		// uma só mensagem
		StringBuilder sbRespostas = new StringBuilder();

		// Armazena a lista de comandos recebidos no seguinte formato:
		// [ 
		//   "/comandoSemParametro", 
		//   "/comandoComUmParametro;parametroUnico", 
		//   "/comandoComDoisParametros;parametro1;Parâmetro com espaço"
		// ]
		List<String> listaComandosRecebidos = retornarListaComandosRecebidos(strComandos);

		// Caso nenhum comando seja reconhecido, aborta a execução, informando que os comandos 
		// não foram encontrados
		if (listaComandosRecebidos == null || listaComandosRecebidos.isEmpty()) {
			// Exibe "Não entendi..."
			sbRespostas.append("Não entendi...");
			// E informa quais são os comandos disponíveis
			sbRespostas.append("\n\n" + GerenciadorComandosBot.getInstancia().getHelp());
			return sbRespostas.toString();
		}

		// Itera cada um dos comandos e executa sua função atrelada
		for (String comandoRecebido : listaComandosRecebidos) {
			int indicePrimeiroSeparador = comandoRecebido.indexOf(";");
			
			// Caso não encontre o primeiro separador, a variável armazenará -1.
			// Desta forma, o comando é chamado sem parâmetros
			if (indicePrimeiroSeparador == -1) {
				sbRespostas.append(GerenciadorComandosBot.getInstancia().tentarExecutarFuncao(comandoRecebido, null));
			} 
			else {
				// Caso o primeiro separador exista, separa o primeiro trecho (comando) do resto (parâmetros)
				String comando = comandoRecebido.substring(0, indicePrimeiroSeparador);
				String strParametros = comandoRecebido.substring(indicePrimeiroSeparador + 1);
				sbRespostas.append(GerenciadorComandosBot.getInstancia().tentarExecutarFuncao(comando, strParametros));
			}
		}
		
		// Flag que sinaliza se pelo menos um comando executado alterou a pilha de comandos pai.
		boolean algumComandoAlterouPilha = false;
		
		// Itera em cada conjunto comandoComParametro para verificar se algum deles alterou a pilha de comandos pai.
		for(String comandoRecebido : listaComandosRecebidos) {
			
			// Recupera o comando, sem nenhum parâmetro
			String comandoSemParametros = comandoRecebido;
			int indicePrimeiroSeparador = comandoRecebido.indexOf(";");
			if (indicePrimeiroSeparador != -1) {
				comandoSemParametros = comandoSemParametros.substring(0, indicePrimeiroSeparador);
			} 
			
			// Caso o comando sem parâmetro seja o "/inicio" ou seja o comando pai mais próximo na pilha,
			// é entendido que a execução do comando alterou a pilha. 
			if (GerenciadorComandosBot.getInstancia().verificarSeComandoAlterouPilha(comandoSemParametros)) {
				algumComandoAlterouPilha = true;
				// Nem precisa continuar procurando, pois o comando atual já sinalizou que alterou a pilha
			}
		}
		
		// Caso tenha algum comando que tenha alterado a pilha, a lista de comandos possíveis,
		// deve ter sido alterada, então exibe a lista de comandos atualizada.
		if (algumComandoAlterouPilha) {
			return GerenciadorComandosBot.getInstancia().retornarStrListaComandos(sbRespostas.toString());
		}

		return sbRespostas.toString();
	}

	private List<String> retornarListaComandosRecebidos(String strComandos) {
		 
		// Contador de aspas para saber se o caracter " " (espaço) é um separador de trechos 
		// ou apenas um caracter de texto
		int contadorAspas = 0;
		
		// Buffer responsável por armazenar cada caracter até que o trecho esteja completo
		StringBuilder sbTrecho = new StringBuilder();
		// Lista de trechos encontrados
		List<String> listaTrechos = new ArrayList<String>();

		// o "for" abaixo itera cada um dos caracteres para alterar o formato:
		
		// Ex1 (antes):  /comandoSemParametro 
		// Ex1 (depois): ["/comandoSemParametro"]
		
		// Ex2 (antes):  /comandoComUmParametro parametroUnico
		// Ex2 (depois): ["/comandoComUmParametro", "parametroUnico"]
		
		// Ex3 (antes):  /comandoComDoisParametros parametro1 "Parâmetro com espaço"
		// Ex3 (depois): ["/comandoComDoisParametros", "parametro1", "Parâmetro com espaço"]
		
		// Ex4 (antes):  /comando1 param1 /comando2 "param2 com espaço"
		// Ex4 (depois): ["/comando1", "param1", "/comando2", "param2 com espaço"]
		
		for (char caracter : strComandos.toCharArray()) {
			switch (caracter) {
			// Aspas, usadas para delimitar uma string com " " (espaços) e evitar que seja 
			// confundido com um " " (espaço) de separação entre trechos.
			// Observação: está lógica trata ambos os tipo de aspas (' e ") da mesma forma,
			// sendo possível abrir um trecho de texto com um tipo e fechar com outro.
			// Ex: /bugFeatureDasAspas 'este é um parâmetro"
			case '"':
			case '\'':
				// Incrementa o número de aspas encontradas até então.
				contadorAspas++;
				break;

			// Espaço, caso não esteja dentro de aspas, finaliza o trecho atual e usa o " " (espaço)
			// como separador de trechos.
			case ' ':
				// Entende-se que o conjunto de caracteres que está entre uma aspa ímpar e uma par 
				// é apenas texto.
				// Desta forma, se o número de aspas encontradas até então for par, significa que o 
				// " " (espaço) encontrado é realmente um separador de trechos.
				// Porém, se o número de aspas encontradas for impar, significa que o " " (espaço) 
				// encontrado é parte de um texto e não deve ser considerado como um separador de trechos.
				if (contadorAspas % 2 == 0) {
					// " " é um separador de trechos, então adiciona o trecho na lista e limpa a variável
					listaTrechos.add(sbTrecho.toString());
					sbTrecho = new StringBuilder();
				} else {
					// " " é apenas um caracter de texto, então apenas adiciona o caracter ao trecho
					sbTrecho.append(caracter);
				}
				break;

			// Demais caracteres, são apenas adicionados ao trecho atual.
			default:
				sbTrecho.append(caracter);
			}
		}
		
		// O que adicionou os trecho até o penúltimo foi o " " (espaço) que veio logo depois. 
		// Como o último trecho não possui um " " (espaço) depois, ele deve ser adicionado separadamente.
		if(sbTrecho.toString().length() > 0){
			listaTrechos.add(sbTrecho.toString());
		}
		
		// Buffer que guarda cada caracter do conjunto comando-parâmetro antes de adicionar na lista.
		StringBuilder sbComandoComParametro = new StringBuilder();
		// Lista de conjuntos comando-parametro.
		List<String> listaComandosComParametros = new ArrayList<String>();

		// o "for" abaixo itera cada um dos caracteres para alterar o formato:
		
		// Ex1 (antes):  ["/comandoSemParametro"]
		// Ex1 (depois): ["/comandoSemParametro"]
		
		// Ex2 (antes):  ["/comandoComUmParametro", "parametroUnico"]
		// Ex2 (depois): ["/comandoComUmParametro;parametroUnico"]
		
		// Ex3 (antes):  ["/comandoComDoisParametros", "parametro1", "Parâmetro com espaço"]
		// Ex3 (depois): ["/comandoComDoisParametros;parametro1;Parâmetro com espaço"]
		
		// Ex4 (antes):  /comando1 param1 /comando2 "param2 com espaço"
		// Ex4 (depois): ["/comando1;param1", "/comando2;param2 com espaço"]
		
		for (String trecho : listaTrechos) {
			
			// Remove qualquer ";", pois este caracter será utilizado como separação entre os trechos
			trecho = trecho.replace(";", "");
			
			// Verifica se o trecho é reconhecido como um comando válido
			if(GerenciadorComandosBot.getInstancia().verificarSeComandoExiste(trecho)) {
				
				// Caso tenha algo concatenado no comandoComParametro, adiciona este "algo" na lista
				// Este "algo" pode ser um comando simples ou um comando com parâmetros.
				// Mas como o "trecho" é considerado como um comando, corta o trecho anterior e o adiciona na lista
				if (sbComandoComParametro.toString().length() > 0) {
					listaComandosComParametros.add(sbComandoComParametro.toString());
				}
				
				// Reinicializa o comandoComParametro com o trecho atual.
				// Todo comandoComParametro é inciado com um comando válido.
				sbComandoComParametro = new StringBuilder(trecho);
				
			}
			else {
				// Se o comandoComParametro não está preenchido significa que o primeiro trecho não é um comando válido.
				// Desta forma, aborta a execução do método, pois o primeiro trecho precisava ser um comando válido.
				if(sbComandoComParametro.toString().isEmpty()) {
					return null;
				}

				// Caso o comandoComParametro tenha algo dentro, concatena um separador ";" e o "trecho" atual;
				sbComandoComParametro.append(";" + trecho);
			}
			
		}
		
		// Da mesma forma que o "if" acima, no mesmo nível de identação (fora do "for"):
		// O que adicionou os trecho até o penúltimo foi o comando válido que veio logo depois. 
		// Como o último trecho não possui um comando depois, ele deve ser adicionado separadamente.
		if(sbComandoComParametro.toString().length() > 0){
			listaComandosComParametros.add(sbComandoComParametro.toString());
		}
		
		return listaComandosComParametros;
	}

	private void printarStatusMensagemEnviada(boolean enviou, String mensagem) {
		// Caso a mensagem tenha sido enviada com sucesso
		if (enviou == true) {
			// Exibe a mensagem no console
			Console.printarMensagemEnviada(mensagem);
			return;
		}

		// Informa erro ao enviar mensagem
		Console.printarErroAoEnviarMensagem(mensagem);
	}
}
