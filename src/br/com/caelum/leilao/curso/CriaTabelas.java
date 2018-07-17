package br.com.caelum.leilao.curso;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

import br.com.caelum.leilao.dao.CriadorDeSessao;

public class CriaTabelas {

	public static void main(String[] args) {
		
		Configuration cfg = new CriadorDeSessao().getConfig();
		SchemaExport se = new SchemaExport(cfg);
		
		se.create(true, true);
	}
	
}
