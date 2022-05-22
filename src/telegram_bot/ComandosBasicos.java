package telegram_bot;

import java.util.ArrayList;
import java.util.List;

public class ComandosBasicos {
	public List<ComandoBot> retornarListaComandos(String comandoPai) {

		// Inicializa a lista de comandos b�sicos (todos ficar�o no contexto comandoPai)
		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		// Comando que retorna o nome dos integrantes do grupo.
		listaComandos.add(new ComandoBot("/nomeDevs", "Exibir o nome do desenvolvedor do bot", a -> {
			StringBuilder sbDevs = new StringBuilder();
			sbDevs.append("\nRM 344507 - Ali Tannouri Neto");
			sbDevs.append("\nRM 344245 - Matheus Ciribeli");
			sbDevs.append("\nRM 345321 - Pedro Henrique Rossi");
			sbDevs.append("\nRM 345763 - Victor Alves Bugueno");
			sbDevs.append("\nRM 344869 - Victor Augusto Dias");

			return String.format("Chatbot da turma 43SCJ criado por:%s", sbDevs.toString());
		}, comandoPai));

		// Comando que retorna a data e hora atual
		listaComandos.add(new ComandoBot("/dataAtual", "Informa a �ltima data de atualiza��o do bot",
				a -> "Agora s�o: " + Console.retornarDataHoraAtual("HH:mm (dd/MM/yyyy)"), comandoPai));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/alfanumericos", "Grupo alfanumericos", a -> {
			GerenciadorComandosBot.getInstancia().adicionarComandoNaPilha("/alfanumericos");
			return "Voc� selecionou o grupo \"alfanumericos\"";
		}, comandoPai));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/letras", "Grupo letras", a -> {
			GerenciadorComandosBot.getInstancia().adicionarComandoNaPilha("/letras");
			return "Voc� selecionou o grupo \"letras\"";
		}, "/alfanumericos"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/letrasMaiusculas", "Grupo letrasmMaiusculas", a -> {
			GerenciadorComandosBot.getInstancia().adicionarComandoNaPilha("/letrasMaiusculas");
			return "Voc� selecionou o grupo \"letrasMaiusculas\"";
		}, "/letras"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/letrasMinusculas", "Grupo letrasMinusculas", a -> {
			GerenciadorComandosBot.getInstancia().adicionarComandoNaPilha("/letrasMinusculas");
			return "Voc� selecionou o grupo \"letrasMinusculas\"";
		}, "/letras"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/numeros", "Grupo numeros", a -> {
			GerenciadorComandosBot.getInstancia().adicionarComandoNaPilha("/numeros");
			return "Voc� selecionou o grupo \"numeros\"";
		}, "/alfanumericos"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/numerosPares", "Grupo numerosPares", a -> {
			GerenciadorComandosBot.getInstancia().adicionarComandoNaPilha("/numerosPares");
			return "Voc� selecionou o grupo \"numerosPares\"";
		}, "/numeros"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/numerosImpares", "Grupo numerosImpares", a -> {
			GerenciadorComandosBot.getInstancia().adicionarComandoNaPilha("/numerosImpares");
			return "Voc� selecionou o grupo \"numerosImpares\"";
		}, "/numeros"));

		// Retorna lista de comandos
		return listaComandos;
	}
}
