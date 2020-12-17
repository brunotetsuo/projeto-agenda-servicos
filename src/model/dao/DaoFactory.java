package model.dao;

import db.DB;
import model.dao.impl.ClientDaoJDBC;

public class DaoFactory {

	public static ClientDao createClientDao() {
		return new ClientDaoJDBC(DB.getConnection());
	}
}
