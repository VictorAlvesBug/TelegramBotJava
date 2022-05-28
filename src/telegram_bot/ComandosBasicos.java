package telegram_bot;

import java.util.ArrayList;
import java.util.List;

public class ComandosBasicos {
	public List<ComandoBot> retornarListaComandos(String comandoPai, GerenciadorComandosBot gerenciadorComandos) {
		// Inicializa a lista de comandos b�sicos (todos ficar�o no contexto comandoPai)
		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		// Comando que retorna a data e hora atual
		listaComandos.add(new ComandoBot("/dataAtual", "Informa a �ltima data de atualiza��o do bot",
				a -> "Agora s�o: " + Console.retornarDataHoraAtual("HH:mm (dd/MM/yyyy)"), comandoPai));

		// Comandos comentados abaixo foram criados apenas para testar a mudan�a de contexto
		// Para reativ�-los, basta descomentar o trecho abaixo.
		/*
		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/alfanumericos", "Grupo alfanumericos", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/alfanumericos");
			return "Voc� selecionou o grupo \"alfanumericos\"";
		}, comandoPai));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/letras", "Grupo letras", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/letras");
			return "Voc� selecionou o grupo \"letras\"";
		}, "/alfanumericos"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/letrasMaiusculas", "Grupo letrasmMaiusculas", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/letrasMaiusculas");
			return "Voc� selecionou o grupo \"letrasMaiusculas\"";
		}, "/letras"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/letrasMinusculas", "Grupo letrasMinusculas", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/letrasMinusculas");
			return "Voc� selecionou o grupo \"letrasMinusculas\"";
		}, "/letras"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/numeros", "Grupo numeros", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/numeros");
			return "Voc� selecionou o grupo \"numeros\"";
		}, "/alfanumericos"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/numerosPares", "Grupo numerosPares", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/numerosPares");
			return "Voc� selecionou o grupo \"numerosPares\"";
		}, "/numeros"));

		// Comando auxiliar usado para testar a mudan�a de contexto e lista de comandos
		// internos dispon�veis.
		listaComandos.add(new ComandoBot("/numerosImpares", "Grupo numerosImpares", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/numerosImpares");
			return "Voc� selecionou o grupo \"numerosImpares\"";
		}, "/numeros"));
		*/
		
		// Retorna lista de comandos
		return listaComandos;
	}
}
