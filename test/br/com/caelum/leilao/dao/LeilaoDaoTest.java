package br.com.caelum.leilao.dao;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.List;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.leilao.builder.LeilaoBuilder;
import br.com.caelum.leilao.dominio.Leilao;
import br.com.caelum.leilao.dominio.Usuario;

public class LeilaoDaoTest {
    private Session session;
    private LeilaoDao leilaoDao;
    private UsuarioDao usuarioDao;

    @Before
    public void antes() {
        session = new CriadorDeSessao().getSession();
        leilaoDao = new LeilaoDao(session);
        usuarioDao = new UsuarioDao(session);
        session.beginTransaction();
    }

    @After
    public void depois() {
        session.getTransaction().rollback();
        session.close();
    }

    @Test
    public void deveContarLeiloesNaoEncerrados() {
        // 1 - cenário
        // criamos um usuario
        Usuario diegoaguiar = new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");

        // criamos os dois leiloes
        Leilao leilao1 = new Leilao("PS 4", 2500.0, diegoaguiar, false);
        Leilao leilao2 = new Leilao("XBox", 1000.0, diegoaguiar, false);
        leilao2.encerra();

        // persistimos todos no banco
        usuarioDao.salvar(diegoaguiar);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        // 2 - acao
        // pedimos o total para o DAO
        long total = leilaoDao.total();
        
        // 3 - verificacao
        assertEquals(1L, total);
    }

    @Test
    public void deveRetornarZeroSeNaoHaLeiloesNovos() {
        // 1 - cenário
        // criamos um usuario
        Usuario diegoaguiar = 
                new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");

        // criamos os dois leiloes
        Leilao leilao1 = new Leilao("PS 4", 2500.0, diegoaguiar, false);
        leilao1.encerra();
        Leilao leilao2 = new Leilao("XBox", 1000.0, diegoaguiar, false);
        leilao2.encerra();

        // persistimos todos no banco
        usuarioDao.salvar(diegoaguiar);
        leilaoDao.salvar(leilao1);
        leilaoDao.salvar(leilao2);

        // 2 - acao
        // pedimos o total para o DAO
        long total = leilaoDao.total();
        
        // 3 - verificacao
        assertEquals(0L, total);
    }

    @Test
    public void deveRetornarLeiloesDeProdutosNovos() {
        // 1 - cenário
        // criamos um usuario
        Usuario diegoaguiar = 
                new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");

        // criamos os dois leiloes
        Leilao leilaoProdutoNovo = new Leilao("PS 4", 2500.0, diegoaguiar, false);
        Leilao leilaoProdutoUsado = new Leilao("XBox", 1000.0, diegoaguiar, true);

        // persistimos todos no banco
        usuarioDao.salvar(diegoaguiar);
        leilaoDao.salvar(leilaoProdutoNovo);
        leilaoDao.salvar(leilaoProdutoUsado);

        // 2 - acao
        List<Leilao> leiloesDeProdutosNovos = leilaoDao.novos();
        
        // 3 - verificacao
        assertEquals(1, leiloesDeProdutosNovos.size());
        assertEquals(leilaoProdutoNovo, leiloesDeProdutosNovos.get(0));
    }

    @Test
    public void deveTrazerSomenteLeiloesAntigos() {
        // 1 - cenário
        // criamos um usuario
        Usuario diegoaguiar = 
                new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");

        // criamos os dois leiloes
        Leilao leilaoRecente = new Leilao("PS 4", 2500.0, diegoaguiar, false);
        Leilao leilaoAntigo = new Leilao("XBox", 1000.0, diegoaguiar, false);

        //criando as datas
        Calendar dataRecente = Calendar.getInstance();
        Calendar dataAntiga = Calendar.getInstance();
        dataAntiga.add(Calendar.DAY_OF_MONTH, -10);
        //setando as datas dos leiloes
        leilaoRecente.setDataAbertura(dataRecente);
        leilaoAntigo.setDataAbertura(dataAntiga);

        // persistimos todos no banco
        usuarioDao.salvar(diegoaguiar);
        leilaoDao.salvar(leilaoRecente);
        leilaoDao.salvar(leilaoAntigo);

        // 2 - acao
        List<Leilao> antigos = leilaoDao.antigos();

        // 3 - verificacao
        assertEquals(1, antigos.size());
        assertEquals("XBox", antigos.get(0).getNome());
    }

    @Test
    public void deveTrazerLeilaoDeSeteDiasAtras() {
	//Sempre que temos um "<", "<=", ">", ">=" ou qualquer outro comparador, é sempre bom testar o limite.

        // 1 - cenário
        // criamos um usuario
        Usuario diegoaguiar = 
                new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");

        // criamos os dois leiloes
        Leilao leilaoDeSeteDias = new Leilao("PS 4", 2500.0, diegoaguiar, false);

        //criando as datas
        Calendar data = Calendar.getInstance();
        Calendar dataDeSeteDias = data;
        dataDeSeteDias.add(Calendar.DAY_OF_MONTH, -7);
        //setando as datas dos leiloes
        leilaoDeSeteDias.setDataAbertura(dataDeSeteDias);

        // persistimos todos no banco
        usuarioDao.salvar(diegoaguiar);
        leilaoDao.salvar(leilaoDeSeteDias);

        // 2 - acao
        List<Leilao> antigos = leilaoDao.antigos();

        // 3 - verificacao (data no Limite)
        assertEquals(1, antigos.size());
        assertEquals("PS 4", antigos.get(0).getNome());
    }

    @Test
    public void deveTrazerLeiloesNaoEncerradosNoPeriodo() {
    	// O teste já prevê as possibilidade envolvendo os dois AND do select
    	// ou seja, está dentro ou fora do período E está encerrado ou nao

        // 1 - cenário
    	//criando intervalo
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
        Calendar fimDoIntervalo = Calendar.getInstance();

        Usuario diegoaguiar = 
                new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");

        // criando os leiloes, cada um com uma data
        Leilao leilaoNoIntervalo = 
                new Leilao("XBox", 700.0, diegoaguiar, false);
        Calendar dataDoLeilaoNoIntervalo = Calendar.getInstance();
        dataDoLeilaoNoIntervalo.add(Calendar.DAY_OF_MONTH, -2);
        leilaoNoIntervalo.setDataAbertura(dataDoLeilaoNoIntervalo);

        Leilao leilaoForaIntervalo = 
                new Leilao("Geladeira", 1700.0, diegoaguiar, false);
        Calendar dataDoLeilaoForaIntervalo = Calendar.getInstance();
        dataDoLeilaoForaIntervalo.add(Calendar.DAY_OF_MONTH, -20);
        leilaoForaIntervalo.setDataAbertura(dataDoLeilaoForaIntervalo);

        // criando leilao no intervalo, porém encerrado
        Leilao leilaoNoIntervaloEncerrado = 
                new Leilao("PS 4", 700.0, diegoaguiar, false);
        leilaoNoIntervaloEncerrado.setDataAbertura(dataDoLeilaoNoIntervalo);
        leilaoNoIntervaloEncerrado.encerra();

        // persistindo os objetos no banco
        usuarioDao.salvar(diegoaguiar);
        leilaoDao.salvar(leilaoNoIntervalo);
        leilaoDao.salvar(leilaoForaIntervalo);
        leilaoDao.salvar(leilaoNoIntervaloEncerrado);

        // 2 - acao
        // invocando o metodo para testar
        List<Leilao> leiloes = 
                leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        // 3 - verificacao
        // garantindo que a query funcionou
        assertEquals(1, leiloes.size());
        assertEquals("XBox", leiloes.get(0).getNome());
    }

    @Test
    public void deveTrazerLeiloesNaoEncerradosNoPeriodoRefatoradoUsandoBUilder() {
    	// O teste já prevê as possibilidade envolvendo os dois AND do select
    	// ou seja, está dentro ou fora do período E está encerrado ou nao

        // 1 - cenário
    	//criando intervalo de datas
        Calendar comecoDoIntervalo = Calendar.getInstance();
        comecoDoIntervalo.add(Calendar.DAY_OF_MONTH, -10);
        Calendar fimDoIntervalo = Calendar.getInstance();

        // criando os leiloes
        Leilao leilaoNoIntervalo = new LeilaoBuilder().diasAtras(2).constroi();

        Leilao leilaoForaIntervalo = new LeilaoBuilder()
					.comNome("Geladeira")
					.comValor(1700.0)
					.diasAtras(20).constroi();

        Leilao leilaoNoIntervaloEncerrado = new LeilaoBuilder()
					.comNome("PS 4")
					.comValor(700.0)
					.diasAtras(2)
					.encerrado().constroi();

        // persistindo os objetos no banco
        leilaoDao.salvar(leilaoNoIntervalo);
        leilaoDao.salvar(leilaoForaIntervalo);
        leilaoDao.salvar(leilaoNoIntervaloEncerrado);

        // 2 - acao
        // invocando o metodo para testar
        List<Leilao> leiloes = leilaoDao.porPeriodo(comecoDoIntervalo, fimDoIntervalo);

        // 3 - verificacao
        // garantindo que a query funcionou
        assertEquals(1, leiloes.size());
        assertEquals("XBox", leiloes.get(0).getNome());
    }

    @Test
    public void deveTrazerLeiloesDisputadosNaoEncerradosNoIntervaloDeValor() {
        // 1 - cenário
    	//criando usuários dos lances
    	Usuario mauricio = new Usuario("Mauricio", "mauricio@aniche.com.br");
        Usuario marcelo = new Usuario("Marcelo", "marcelo@aniche.com.br");

        // criando os leiloes
        Leilao leilaoNoIntervaloSemLance = new LeilaoBuilder()
						.comValor(3000.0).diasAtras(2).constroi();

        Leilao leilaoNoIntervaloComLance = new LeilaoBuilder().comValor(3000.0)
						.comLance(Calendar.getInstance(), mauricio, 3000.0)
                		.comLance(Calendar.getInstance(), marcelo, 3100.0)
						.constroi();

        Leilao leilaoDisputadoNoIntervalo = new LeilaoBuilder().comValor(3000.0)
						.comLance(Calendar.getInstance(), mauricio, 3000.0)
						.comLance(Calendar.getInstance(), marcelo, 3100.0)
						.comLance(Calendar.getInstance(), mauricio, 3200.0)
						.comLance(Calendar.getInstance(), marcelo, 3300.0)
						.comLance(Calendar.getInstance(), mauricio, 3400.0)
						.comLance(Calendar.getInstance(), marcelo, 3500.0)
						.constroi();

        Leilao leilaoDisputadoNoIntervaloEncerrado = new LeilaoBuilder().comValor(3000.0)
						.comLance(Calendar.getInstance(), mauricio, 3000.0)
						.comLance(Calendar.getInstance(), marcelo, 3100.0)
						.comLance(Calendar.getInstance(), mauricio, 3200.0)
						.comLance(Calendar.getInstance(), marcelo, 3300.0)
						.comLance(Calendar.getInstance(), mauricio, 3400.0)
						.comLance(Calendar.getInstance(), marcelo, 3500.0)
						.encerrado().constroi();

        Leilao leilaoForaIntervaloComLance = new LeilaoBuilder().comValor(1700.0)
						.comLance(Calendar.getInstance(), mauricio, 3000.0)
		       			.comLance(Calendar.getInstance(), marcelo, 3100.0)
						.constroi();

        Leilao leilaoForaIntervaloDisputado = new LeilaoBuilder().comValor(1700.0)
						.comLance(Calendar.getInstance(), mauricio, 3000.0)
						.comLance(Calendar.getInstance(), marcelo, 3100.0)
						.comLance(Calendar.getInstance(), mauricio, 3200.0)
						.comLance(Calendar.getInstance(), marcelo, 3300.0)
						.comLance(Calendar.getInstance(), mauricio, 3400.0)
						.comLance(Calendar.getInstance(), marcelo, 3500.0)
						.constroi();

        Leilao leilaoForaIntervaloDisputadoEncerrado = new LeilaoBuilder().comValor(1700.0)
						.comLance(Calendar.getInstance(), mauricio, 3000.0)
						.comLance(Calendar.getInstance(), marcelo, 3100.0)
						.comLance(Calendar.getInstance(), mauricio, 3200.0)
						.comLance(Calendar.getInstance(), marcelo, 3300.0)
						.comLance(Calendar.getInstance(), mauricio, 3400.0)
						.comLance(Calendar.getInstance(), marcelo, 3500.0)
						.encerrado().constroi();


        // persistindo os objetos no banco
        usuarioDao.salvar(marcelo);
        usuarioDao.salvar(mauricio);
        leilaoDao.salvar(leilaoNoIntervaloSemLance);
        leilaoDao.salvar(leilaoNoIntervaloComLance);
        leilaoDao.salvar(leilaoDisputadoNoIntervalo);
        leilaoDao.salvar(leilaoDisputadoNoIntervaloEncerrado);
        leilaoDao.salvar(leilaoForaIntervaloComLance);
        leilaoDao.salvar(leilaoForaIntervaloDisputado);
        leilaoDao.salvar(leilaoForaIntervaloDisputadoEncerrado);

		// 2 - acao
        // invocando o metodo para testar
        List<Leilao> leiloes = leilaoDao.disputadosEntre(2500, 3500);

        // 3 - verificacao
        // garantindo que a query funcionou
        assertEquals(1, leiloes.size());
        assertEquals(3000.0, leiloes.get(0).getValorInicial(), 0.00001);
    }

    @Test
    public void listaSomenteOsLeiloesDoUsuarioSemRepetirLeiloes() throws Exception {
        // 1 - cenário
    	//criando usuários dos lances
    	Usuario comprador = new Usuario("Diego", "diego@gmail.com.br");
    	Usuario comprador2 = new Usuario("Aguiar", "aguiar@gmail.com.br");
    	// criando os leiloes
        Leilao leilao = new LeilaoBuilder()
            .comValor(50.0)
            .comLance(Calendar.getInstance(), comprador, 100.0)
            .comLance(Calendar.getInstance(), comprador2, 200.0)
            .comLance(Calendar.getInstance(), comprador, 250.0)
            .constroi();
        Leilao leilao2 = new LeilaoBuilder()
            .comValor(250.0)
            .comLance(Calendar.getInstance(), comprador, 50.0)
            .comLance(Calendar.getInstance(), comprador2, 100.0)
            .constroi();
        // persistindo os objetos no banco
        usuarioDao.salvar(comprador);
        usuarioDao.salvar(comprador2);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        // 2 - acao
        // invocando o metodo para testar
        List<Leilao> leiloes = leilaoDao.listaLeiloesDoUsuario(comprador);

        // 3 - verificacao
        // garantindo que a query funcionou
        assertEquals(2, leiloes.size());
        assertEquals(leilao, leiloes.get(0));
    }

    @Test
    public void devolveAMediaDoValorInicialDosLeiloesQueOUsuarioParticipou(){
    	// 1 - cenário
    	//criando usuário do lance
    	Usuario comprador = new Usuario("Diego", "diego@gmail.com.br");
    	Usuario dono = new Usuario("Aguiar", "aguiar@gmail.com.br");
    	// criando os leiloes
        Leilao leilao = new LeilaoBuilder()
            .comDono(dono)
            .comValor(50.0)
            .comLance(Calendar.getInstance(), comprador, 100.0)
            .comLance(Calendar.getInstance(), comprador, 200.0)
            .constroi();
        Leilao leilao2 = new LeilaoBuilder()
            .comDono(dono)
            .comValor(250.0)
            .comLance(Calendar.getInstance(), comprador, 100.0)
            .constroi();

        // persistindo os objetos no banco
        usuarioDao.salvar(comprador);
        leilaoDao.salvar(leilao);
        leilaoDao.salvar(leilao2);

        // 2 - acao
        // invocando o metodo para testar
        Double media = leilaoDao.getValorInicialMedioDoUsuario(comprador);

        // 3 - verificacao
        // garantindo que a query funcionou
        assertEquals(150.0, media, 0.001);
    }
    
    @Test
    public void deveDeletarUmLeilao() {
        // 1 - cenário
        Usuario usuario = new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");
        usuarioDao.salvar(usuario);

        Leilao leilao = new LeilaoBuilder()
            .comDono(usuario)
            .comLance(Calendar.getInstance(), usuario, 10000.0)
            .constroi();
        leilaoDao.salvar(leilao);
        // envia tudo para o banco de dados  
        session.flush();

        // 2 - ação
        leilaoDao.deleta(leilao);

        // 3 - verificação
        assertNull(leilaoDao.porId(leilao.getId()));
    }
}
