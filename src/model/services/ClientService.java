package model.services;

import java.util.List;

import model.dao.ClientDao;
import model.dao.DaoFactory;
import model.entities.Client;

public class ClientService {
	
	private ClientDao dao = DaoFactory.createClientDao();
	
	public List<Client> findAll() {
		return dao.findAll();
	}
	
	public void saveOrUpdate(Client obj) {
		if (obj.getId() == null) {
			dao.insert(obj);
		}
		else {
			dao.update(obj);
		}
	}
	
	public void remove(Client obj) {
		dao.deleteById(obj.getId());
	}
	
	public Client findByName(String name) {
		return dao.findByName(name);
	}
}