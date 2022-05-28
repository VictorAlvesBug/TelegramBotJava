package telegram_bot;

import java.util.ArrayList;
import java.util.List;

public class ComandosBasicos {
	public List<ComandoBot> retornarListaComandos(String comandoPai, GerenciadorComandosBot gerenciadorComandos) {
		// Inicializa a lista de comandos básicos (todos ficarão no contexto comandoPai)
		List<ComandoBot> listaComandos = new ArrayList<ComandoBot>();

		// Comando que retorna a data e hora atual
		listaComandos.add(new ComandoBot("/dataAtual", "Informa a última data de atualização do bot",
				a -> "Agora são: " + Console.retornarDataHoraAtual("HH:mm (dd/MM/yyyy)"), comandoPai));

		// Comandos comentados abaixo foram criados apenas para testar a mudança de contexto
		// Para reativá-los, basta descomentar o trecho abaixo.
		/*
		// Comando auxiliar usado para testar a mudança de contexto e lista de comandos
		// internos disponíveis.
		listaComandos.add(new ComandoBot("/alfanumericos", "Grupo alfanumericos", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/alfanumericos");
			return "Você selecionou o grupo \"alfanumericos\"";
		}, comandoPai));

		// Comando auxiliar usado para testar a mudança de contexto e lista de comandos
		// internos disponíveis.
		listaComandos.add(new ComandoBot("/letras", "Grupo letras", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/letras");
			return "Você selecionou o grupo \"letras\"";
		}, "/alfanumericos"));

		// Comando auxiliar usado para testar a mudança de contexto e lista de comandos
		// internos disponíveis.
		listaComandos.add(new ComandoBot("/letrasMaiusculas", "Grupo letrasmMaiusculas", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/letrasMaiusculas");
			return "Você selecionou o grupo \"letrasMaiusculas\"";
		}, "/letras"));

		// Comando auxiliar usado para testar a mudança de contexto e lista de comandos
		// internos disponíveis.
		listaComandos.add(new ComandoBot("/letrasMinusculas", "Grupo letrasMinusculas", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/letrasMinusculas");
			return "Você selecionou o grupo \"letrasMinusculas\"";
		}, "/letras"));

		// Comando auxiliar usado para testar a mudança de contexto e lista de comandos
		// internos disponíveis.
		listaComandos.add(new ComandoBot("/numeros", "Grupo numeros", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/numeros");
			return "Você selecionou o grupo \"numeros\"";
		}, "/alfanumericos"));

		// Comando auxiliar usado para testar a mudança de contexto e lista de comandos
		// internos disponíveis.
		listaComandos.add(new ComandoBot("/numerosPares", "Grupo numerosPares", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/numerosPares");
			return "Você selecionou o grupo \"numerosPares\"";
		}, "/numeros"));

		// Comando auxiliar usado para testar a mudança de contexto e lista de comandos
		// internos disponíveis.
		listaComandos.add(new ComandoBot("/numerosImpares", "Grupo numerosImpares", a -> {
			gerenciadorComandos.adicionarComandoNaPilha("/numerosImpares");
			return "Você selecionou o grupo \"numerosImpares\"";
		}, "/numeros"));
		*/
		
		// Retorna lista de comandos
		return listaComandos;
	}
}
