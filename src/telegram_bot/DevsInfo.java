package telegram_bot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DevsInfo {
	public List<ComandoBot> retornarListaComandos(String comandoPai, GerenciadorComandosBot gerenciadorComandos) {

		// Inicializa a lista de comandos com as informações dos devs
		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		// Comando que retorna o nome dos integrantes do grupo.
		listaComandos.add(new ComandoBot("/devs", "Informações sobre os desenvolvedores do bot", a -> {
			// Entra no contexto do comando /devs
			gerenciadorComandos.adicionarComandoNaPilha("/devs");

			// Constrói o conteúdo personalizado, para que não sejam exibidos como meros
			// comandos disponíveis
			StringBuilder sbDevs = new StringBuilder();
			sbDevs.append("\n\nChatbot da turma 43SCJ criado por:");
			sbDevs.append("\n/RM344507 - Ali Tannouri Neto");
			sbDevs.append("\n/RM344245 - Matheus Ciribeli");
			sbDevs.append("\n/RM345321 - Pedro Henrique Rossi");
			sbDevs.append("\n/RM345763 - Victor Alves Bugueno");
			sbDevs.append("\n/RM344869 - Victor Augusto Dias");

			gerenciadorComandos.retornarComandoBotPai().setConteudoPersonalizado(sbDevs.toString());
			return "";
		}, comandoPai));

		// Informações sobre os devs (Ali Tannouri Neto)
		listaComandos.add(retornarComandoBotDevInfo(344507, "Ali Tannouri Neto",
				"Analista de planejamento e pricing senior na Itaú Unibanco", LocalDate.of(1995, 1, 5),
				"https://www.linkedin.com/in/ali-tannouri-neto-12aaa6110/"));

		// Informações sobre os devs (Matheus Ciribeli)
		listaComandos
				.add(retornarComandoBotDevInfo(344245, "Matheus Ciribeli", "EDI Development | Full Stack Developer",
						LocalDate.of(1993, 8, 18), "https://www.linkedin.com/in/matheus-ciribeli-02226089/"));

		// Informações sobre os devs (Pedro Henrique Rossi)
		listaComandos.add(retornarComandoBotDevInfo(345321, "Pedro Henrique Rossi", "IT Analyst | JAVA | BTG Pactual",
				LocalDate.of(2000, 9, 9), "https://www.linkedin.com/in/pedrohrossi99/"));

		// Informações sobre os devs (Victor Alves Bugueno)
		listaComandos.add(retornarComandoBotDevInfo(345763, "Victor Alves Bugueno", "Desenvolvedor de Software Pleno",
				LocalDate.of(1999, 1, 10), "https://www.linkedin.com/in/victor-alves-bugueno-122438144/"));

		// Informações sobre os devs (Victor Augusto Dias)
		listaComandos.add(retornarComandoBotDevInfo(344869, "Victor Augusto Dias",
				"Administrador de sistema na NM ENGENHARIA E CONSTRUÇÕES LTDA.", LocalDate.of(1992, 6, 1),
				"https://www.linkedin.com/in/victor-dias-6b505275/"));

		// Retorna lista de comandos
		return listaComandos;
	}

	// Retorna as informações de RM, nome, idade, cargo/empresa e linkedin de cada
	// um dos devs
	private static ComandoBot retornarComandoBotDevInfo(int rm, String nome, String descricao, LocalDate dataNascimento,
			String linkedin) {
		String comando = "/RM" + rm;
		return new ComandoBot(comando, nome, a -> {
			LocalDate dataAtual = LocalDate.now();
			int idade = dataAtual.compareTo(dataNascimento);

			StringBuilder sbDevInfo = new StringBuilder();
			sbDevInfo.append("Olá, meu nome é " + nome + " (RM: " + rm + ") e tenho " + idade + " anos.");
			sbDevInfo.append("\n" + descricao);
			sbDevInfo.append("\n\nPara mais informações, Linkedin: " + linkedin);
			return sbDevInfo.toString();
		}, "/devs");
	}
}
