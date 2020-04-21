package de.leonso.xfl.jsf;

import java.io.Serializable;

import de.leonso.core.notes.LINotesFactoryProvider;
import de.leonso.core.notes.LNativeNotesSessionProvider;
import de.leonso.core.notes.LSessionProviderNotesJsf;
import de.leonso.core.notes.api.NotesFactory;
import de.leonso.core.notes.api.SessionWrapper;

public class XflJsfEngine extends de.leonso.xfl.XflEngine implements Serializable {
	private static final long serialVersionUID = 1L;

	public XflJsfEngine() {
		super(new LINotesFactoryProvider() {
			private static final long serialVersionUID = 1L;

			private LNativeNotesSessionProvider sessionProvider = new LSessionProviderNotesJsf();

			@Override
			public SessionWrapper getSession() {
				return getNotesFactory().getSession();
			}

			@Override
			public SessionWrapper getSessionAsSigner() {
				return getNotesFactory().getSessionAsSigner();
			}

			@Override
			public boolean isWebSession() {
				return true;
			}

			@Override
			public void close() {
				if (!closed) {
					sessionProvider.close();
					sessionProvider = null;
					if (notesFactory != null) {
						notesFactory.close();
						notesFactory = null;
					}
					closed = true;
				}
			}

			private boolean closed = false;

			@Override
			public boolean isClosed() {
				return closed;
			}

			private NotesFactory notesFactory;

			@Override
			public NotesFactory getNotesFactory() {
				if (notesFactory == null) {
					notesFactory = new NotesFactory(sessionProvider);
				}
				return notesFactory;
			}
		});
	}

}
