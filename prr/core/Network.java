package prr.core;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;

import prr.core.exception.UnrecognizedEntryException;
import prr.core.terminal.BasicTerminal;
import prr.core.terminal.FancyTerminal;
import prr.core.terminal.Terminal;
import prr.util.StringComparator;
import prr.util.Visitable;
import prr.util.Visitor;
import prr.core.exception.DuplicateTerminalKeyException;
import prr.core.exception.DuplicateClientKeyException;
import prr.core.client.Client;
import prr.core.exception.UnknownClientKeyException;
import prr.core.exception.UnknownTerminalKeyException;

import java.io.IOException;

// FIXME add more import if needed (cannot import from pt.tecnico or prr.app)

/**
 * Class Store implements a store.
 */
public class Network implements Serializable {

	/** Serial number for serialization. */
	private static final long serialVersionUID = 202210161305L;

	private Map<String, Terminal> _terminals =
			new TreeMap<String, Terminal>(new StringComparator());

	private Map<String, Client> _clients = new TreeMap<String, Client>();

	/**
	 * Adds a new terminal to the app.
	 * 
	 * @param type Terminal type
	 * @param key Terminal key
	 * @param client Terminal's client's key
	 * @param state Terminals state
	 * @throws IllegalArgumentException
	 * @throws UnknownClientKeyException
	 */
	public Terminal registerTerminal(String type, String key, String client)
			throws IllegalArgumentException, UnknownClientKeyException,
			DuplicateTerminalKeyException {
		Terminal newTerm;
		Terminal.checkKey(key);
		Client owner = this.getClient(client);

		switch(type) {
			case "BASIC" -> newTerm = new BasicTerminal(key, owner);
			case "FANCY" -> newTerm = new FancyTerminal(key, owner);
			default -> throw new IllegalArgumentException();
		}
		this.addTerminal(newTerm);
		owner.addTerminal(newTerm, key);
		return newTerm;
	}

	public Client getClient(String key) throws UnknownClientKeyException {
		Client client = this._clients.get(key);

		if (client == null) {
			throw new UnknownClientKeyException(key);
		}
		return client;
	}

	public Terminal getTerminal(String key) throws UnknownTerminalKeyException {
		Terminal terminal = this._terminals.get(key);

		if (terminal == null) {
			throw new UnknownTerminalKeyException(key);
		}
		return terminal;
	}

	public Collection<Client> getAllClients() {
		return this._clients.values();
	}

	public Collection<Terminal> getAllTerminals() {
		return this._terminals.values();
	}

	public int getClientCount() {
		return this._clients.size();
	}

	public int getTerminalCount() {
		return this._terminals.size();
	}

	private void addTerminal(Terminal terminal)
			throws DuplicateTerminalKeyException {
		String key = terminal.getKey();

		if (this._terminals.keySet().contains(key)) {
			throw new DuplicateTerminalKeyException();
		} else {
			this._terminals.put(key, terminal);
		}
	}

	private void addClient(Client client) throws DuplicateClientKeyException {
		String key = client.getKey();

		if (this._clients.keySet().contains(key)) {
			throw new DuplicateClientKeyException();
		} else {
			this._clients.put(key, client);
		}
	}

	public Client registerClient(String key, String name, int taxId) 
			throws DuplicateClientKeyException {
		Client newClient = new Client(key, name, taxId);

		this.addClient(newClient);
		return newClient;
	}

	public void addFriend(String terminalKey, String friendKey) 
			throws UnknownTerminalKeyException, IllegalArgumentException {
		if (terminalKey.equals(friendKey)) {
			throw new IllegalArgumentException();
		}
		Terminal terminal = getTerminal(terminalKey);
		Terminal friend = getTerminal(friendKey);

		terminal.addFriend(friend);
	}

	public <T> void visitAll(Visitor<T> visitor,
			Collection<? extends Visitable> col,
			Predicate<Visitable> valid) {

		for (Visitable element : col) {
			if (valid.test(element)) {
				element.accept(visitor);
			}
		}
	}

}


