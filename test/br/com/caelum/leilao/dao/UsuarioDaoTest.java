package br.com.caelum.leilao.dao;

import static org.junit.Assert.*;

import org.hibernate.Session;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import br.com.caelum.leilao.dominio.Usuario;

public class UsuarioDaoTest {
	
	private Session session;
	private UsuarioDao usuarioDao;

	@Before
	public void setUp() {
		// executa este m�todo antes da execu��o de cada teste
		// instancia tudo que � necess�rio para o cenario dos testes
		session = new CriadorDeSessao().getSession();
		usuarioDao = new UsuarioDao(session);
	}
	
	@After
	public void finaliza() {
		// executa este m�todo ap�s a execu��o de cada teste
		// Utilizado quando os testes consomem recursos que precisam ser finalizados
		session.close();
	}
	
	@Test
	public void deveEncontrarPeloNomeEEmail() {
		// 1 - cen�rio
		UsuarioDao usuarioDao = new UsuarioDao(session);
		usuarioDao.salvar(new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com"));

		// 2 - Acao
		Usuario usuarioBD = usuarioDao.porNomeEEmail("Diego Aguiar", "diego.fernandes.aguiar@gmail.com");

		// 3 - verificacao
		assertEquals("Diego Aguiar", usuarioBD.getNome());
		assertEquals("diego.fernandes.aguiar@gmail.com", usuarioBD.getEmail());
	}
	
	@Test
	public void deveRetornarNuloSeNaoEncontrarUsuario() {
		// 1 - cen�rio
		UsuarioDao usuarioDao = new UsuarioDao(session);

		// 2 - Acao
		Usuario usuarioBD = usuarioDao.porNomeEEmail("Jo�o Joaquim", "joao@joaquim.com.br");

		// 3 - verificacao
		assertNull(usuarioBD);
	}
	
	@Test
	public void deveDeletarUmUsuario() {
	    // 1 - cen�rio
	    Usuario usuario = new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");
	    usuarioDao.salvar(usuario);

	    // 2 - a��o
	    usuarioDao.deletar(usuario);
	    // envia tudo para o banco de dados        
	    session.flush();

	    // 3 - verifica��o
	    Usuario usuarioNoBanco = usuarioDao.porNomeEEmail("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");
	    assertNull(usuarioNoBanco);
	}

	@Test
	public void deveAtualizarUmUsuario() {
	    // 1 - cen�rio
	    Usuario usuario = new Usuario("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");
	    usuarioDao.salvar(usuario);
	    session.flush(); // envia tudo para o banco de dados 

	    // 2 - a��o
	    usuario.setNome("Diego Aguiar2");
	    usuarioDao.update(usuario);
	    session.flush(); // envia tudo para o banco de dados        

	    // 3 - verifica��o
	    Usuario usuarioAntigo = usuarioDao.porNomeEEmail("Diego Aguiar", "diego.fernandes.aguiar@gmail.com.br");
	    assertNull(usuarioAntigo); //nao achou
	    Usuario usuarioAtualizado = usuarioDao.porNomeEEmail("Diego Aguiar2", "diego.fernandes.aguiar@gmail.com.br");    
	    assertNotNull(usuarioAtualizado); //achou
	    assertEquals(usuario, usuarioAtualizado); 
	}   
}
