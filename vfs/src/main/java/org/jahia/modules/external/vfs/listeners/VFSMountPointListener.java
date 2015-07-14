package org.jahia.modules.external.vfs.listeners;

import org.jahia.services.content.MountPointListener;

/**
 * Created by qlamerand on 13/07/15.
 */
public class VFSMountPointListener extends MountPointListener {

    @Override
    public String[] getNodeTypes() {
        return new String[] {"jnt:vfsMountPoint"};
    }

    @Override
    public String getPath() {
        return null;
    }
}
