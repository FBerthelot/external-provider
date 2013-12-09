/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2013 Jahia Solutions Group SA. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL (or any later version), you may redistribute this Program in connection
 * with Free/Libre and Open Source Software ("FLOSS") applications as described
 * in Jahia's FLOSS exception. You should have received a copy of the text
 * describing the FLOSS exception, and it is also available here:
 * http://www.jahia.com/license
 *
 * Commercial and Supported Versions of the program (dual licensing):
 * alternatively, commercial and supported versions of the program may be used
 * in accordance with the terms and conditions contained in a separate
 * written agreement between you and Jahia Solutions Group SA.
 *
 * If you are unsure which license is appropriate for your use,
 * please contact the sales department at sales@jahia.com.
 */

package org.jahia.modules.external;

import org.jahia.modules.external.query.ExternalQueryManager;
import org.xml.sax.ContentHandler;

import javax.jcr.*;
import javax.jcr.lock.Lock;
import javax.jcr.lock.LockException;
import javax.jcr.lock.LockManager;
import javax.jcr.nodetype.*;
import javax.jcr.observation.EventJournal;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.EventListenerIterator;
import javax.jcr.observation.ObservationManager;
import javax.jcr.query.QueryManager;
import javax.jcr.version.Version;
import javax.jcr.version.VersionException;
import javax.jcr.version.VersionManager;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of the {@link javax.jcr.Workspace} for the {@link org.jahia.modules.external.ExternalData}.
 * User: toto
 * Date: Apr 23, 2008
 * Time: 11:45:56 AM
 *
 */
public class ExternalWorkspaceImpl implements Workspace {
    private ExternalSessionImpl externalSession;
    private String workspaceName;

    public ExternalWorkspaceImpl(ExternalSessionImpl session, String workspaceName) {
        this.externalSession = session;
        this.workspaceName = workspaceName;
    }

    public ExternalSessionImpl getSession() {
        return externalSession;
    }

    public String getName() {
        return workspaceName;
    }

    public void copy(String s, String s1) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void copy(String s, String s1, String s2) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void clone(String s, String s1, String s2, boolean b) throws NoSuchWorkspaceException, ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public void move(String source, String dest) throws ConstraintViolationException, VersionException, AccessDeniedException, PathNotFoundException, ItemExistsException, LockException, RepositoryException {
        if (externalSession.getRepository().getDataSource() instanceof ExternalDataSource.Writable) {
            ((ExternalDataSource.Writable) externalSession.getRepository().getDataSource()).move(source, dest);
        } else {
            throw new UnsupportedRepositoryOperationException();
        }
    }

    public void restore(Version[] versions, boolean b) throws ItemExistsException, UnsupportedRepositoryOperationException, VersionException, LockException, InvalidItemStateException, RepositoryException {
        throw new UnsupportedRepositoryOperationException();
    }

    public QueryManager getQueryManager() throws RepositoryException {
        if (externalSession.getRepository().getDataSource() instanceof ExternalDataSource.Searchable) {
            return new ExternalQueryManager(this);
        }
        return null;
    }

    public NamespaceRegistry getNamespaceRegistry() throws RepositoryException {
        return externalSession.getRepository().getNamespaceRegistry();
    }

    public NodeTypeManager getNodeTypeManager() throws RepositoryException {
        return new NodeTypeManager() {
            public NodeType getNodeType(String s) throws NoSuchNodeTypeException, RepositoryException {
                return null;
            }

            public NodeTypeIterator getAllNodeTypes() throws RepositoryException {
                return null;
            }

            public NodeTypeIterator getPrimaryNodeTypes() throws RepositoryException {
                return null;
            }

            public NodeTypeIterator getMixinNodeTypes() throws RepositoryException {
                return null;
            }

            public boolean hasNodeType(String name) throws RepositoryException {
                return false;
            }

            public NodeTypeTemplate createNodeTypeTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
                return null;
            }

            public NodeTypeTemplate createNodeTypeTemplate(NodeTypeDefinition ntd) throws UnsupportedRepositoryOperationException, RepositoryException {
                return null;
            }

            public NodeDefinitionTemplate createNodeDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
                return null;
            }

            public PropertyDefinitionTemplate createPropertyDefinitionTemplate() throws UnsupportedRepositoryOperationException, RepositoryException {
                return null;
            }

            public NodeType registerNodeType(NodeTypeDefinition ntd, boolean allowUpdate) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {
                return null;
            }

            public NodeTypeIterator registerNodeTypes(NodeTypeDefinition[] ntds, boolean allowUpdate) throws InvalidNodeTypeDefinitionException, NodeTypeExistsException, UnsupportedRepositoryOperationException, RepositoryException {
                return null;
            }

            public void unregisterNodeType(String name) throws UnsupportedRepositoryOperationException, NoSuchNodeTypeException, RepositoryException {

            }

            public void unregisterNodeTypes(String[] names) throws UnsupportedRepositoryOperationException, NoSuchNodeTypeException, RepositoryException {

            }
        };
    }

    public ObservationManager getObservationManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new ObservationManager() {
            public void addEventListener(EventListener eventListener, int i, String s, boolean b, String[] strings, String[] strings1, boolean b1) throws RepositoryException {

            }

            public void removeEventListener(EventListener eventListener) throws RepositoryException {

            }

            public EventListenerIterator getRegisteredEventListeners() throws RepositoryException {
                return null;
            }

            public void setUserData(String userData) throws RepositoryException {

            }

            public EventJournal getEventJournal() throws RepositoryException {
                return null;
            }

            public EventJournal getEventJournal(int i, String s, boolean b, String[] strings, String[] strings1) throws RepositoryException {
                return null;
            }
        };
    }

    public String[] getAccessibleWorkspaceNames() throws RepositoryException {
        return new String[0];
    }

    public ContentHandler getImportContentHandler(String s, int i) throws PathNotFoundException, ConstraintViolationException, VersionException, LockException, AccessDeniedException, RepositoryException {
        return null;
    }

    public void importXML(String s, InputStream inputStream, int i) throws IOException, PathNotFoundException, ItemExistsException, ConstraintViolationException, InvalidSerializedDataException, LockException, AccessDeniedException, RepositoryException {

    }

    public LockManager getLockManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        final Session extensionSession = externalSession.getExtensionSession();
        if (extensionSession == null) {
            return null;
        }
        final LockManager extensionLockMgr =  extensionSession.getWorkspace().getLockManager();
        return new LockManager() {

            @Override
            public void addLockToken(String lockToken) throws LockException, RepositoryException {
                extensionLockMgr.addLockToken(lockToken);
            }

            @Override
            public Lock getLock(String absPath) throws PathNotFoundException, LockException, AccessDeniedException, RepositoryException {
                Node  n = ((ExternalNodeImpl) externalSession.getNode(absPath)).getExtensionNode(false);
                if (n != null)  {
                    return extensionLockMgr.getLock(n.getPath());
                } else {
                    throw new PathNotFoundException("unable to get node " + absPath);
                }
            }

            @Override
            public String[] getLockTokens() throws RepositoryException {
                return extensionLockMgr.getLockTokens();
            }

            @Override
            public boolean holdsLock(String absPath) throws PathNotFoundException, RepositoryException {
                Node  n = ((ExternalNodeImpl) externalSession.getNode(absPath)).getExtensionNode(false);
                return n!=null && extensionLockMgr.holdsLock(n.getPath());
            }

            @Override
            public Lock lock(final String absPath,final boolean isDeep,final  boolean isSessionScoped, final long timeoutHint,final  String ownerInfo) throws LockException, PathNotFoundException, AccessDeniedException, InvalidItemStateException, RepositoryException {
                Node  n = ((ExternalNodeImpl) externalSession.getNode(absPath)).getExtensionNode(true);
                extensionSession.save();
                return extensionLockMgr.lock(n.getPath(),isDeep,isSessionScoped,timeoutHint,ownerInfo);
            }

            @Override
            public boolean isLocked(String absPath) throws PathNotFoundException, RepositoryException {
                Node  n = ((ExternalNodeImpl) externalSession.getNode(absPath)).getExtensionNode(false);
                return n != null && extensionLockMgr.isLocked(n.getPath());
            }

            @Override
            public void removeLockToken(String lockToken) throws LockException, RepositoryException {
                extensionLockMgr.removeLockToken(lockToken);
            }

            @Override
            public void unlock(String absPath) throws PathNotFoundException, LockException, AccessDeniedException, InvalidItemStateException, RepositoryException {
                Node  n = ((ExternalNodeImpl) externalSession.getNode(absPath)).getExtensionNode(false);
                if (n!=null) {
                    extensionLockMgr.unlock(n.getPath());
                }
            }
        };
    }

    public VersionManager getVersionManager() throws UnsupportedRepositoryOperationException, RepositoryException {
        return new ExternalVersionManagerImpl();
    }

    public void createWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Workspace creation is not supported by the external Repository");
    }

    public void createWorkspace(String name, String srcWorkspace) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Workspace creation is not supported by the external Repository");
    }

    public void deleteWorkspace(String name) throws AccessDeniedException, UnsupportedRepositoryOperationException, NoSuchWorkspaceException, RepositoryException {
        throw new UnsupportedRepositoryOperationException("Workspace deleting is not supported by the external Repository");
    }
}
