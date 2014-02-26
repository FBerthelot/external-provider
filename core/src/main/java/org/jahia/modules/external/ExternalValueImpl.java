/**
 * This file is part of Jahia, next-generation open source CMS:
 * Jahia's next-generation, open source CMS stems from a widely acknowledged vision
 * of enterprise application convergence - web, search, document, social and portal -
 * unified by the simplicity of web content management.
 *
 * For more information, please visit http://www.jahia.com.
 *
 * Copyright (C) 2002-2014 Jahia Solutions Group SA. All rights reserved.
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

import org.apache.jackrabbit.util.ISO8601;

import javax.jcr.*;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Implementation of the {@link javax.jcr.Value} for the {@link org.jahia.modules.external.ExternalData}.
 * User: loom
 * Date: Aug 12, 2010
 * Time: 3:04:33 PM
 */
public class ExternalValueImpl implements Value {

    private Object value;
    private int type;

    public ExternalValueImpl(String value) {
        this(value, PropertyType.STRING);
    }

    public ExternalValueImpl(Binary value) {
        this(value, PropertyType.BINARY);
    }

    public ExternalValueImpl(long value) {
        this(value, PropertyType.LONG);
    }

    public ExternalValueImpl(double value) {
        this(value, PropertyType.DOUBLE);
    }

    public ExternalValueImpl(BigDecimal value) {
        this(value, PropertyType.DECIMAL);
    }

    public ExternalValueImpl(Calendar value) {
        this(value, PropertyType.DATE);
    }

    public ExternalValueImpl(boolean value) {
        this(value, PropertyType.BOOLEAN);
    }

    public ExternalValueImpl(Node value, boolean weakReference) throws RepositoryException {
        if (weakReference) {
            this.value = value.getIdentifier();
            this.type = PropertyType.WEAKREFERENCE;
        } else {
            this.value = value.getIdentifier();
            this.type = PropertyType.REFERENCE;
        }
    }

    public ExternalValueImpl(Object value, int type) {
        this.value = value;
        this.type = type;
    }

    public String getString() throws ValueFormatException, IllegalStateException, RepositoryException {
        if (value instanceof Calendar) {
            return ISO8601.format((Calendar) value);
        }
        return value.toString();
    }

    public InputStream getStream() throws RepositoryException {
        if (value instanceof Binary) {
            return ((Binary) value).getStream();
        }
        throw new ValueFormatException();
    }

    public Binary getBinary() throws RepositoryException {
        if (value instanceof Binary) {
            return (Binary) value;
        }
        throw new ValueFormatException();
    }

    public long getLong() throws ValueFormatException, RepositoryException {
        if (value instanceof Long) {
            return ((Long) value);
        }
        try {
            return Long.parseLong(getString());
        } catch (NumberFormatException e) {
            throw new ValueFormatException(e);
        }
    }

    public double getDouble() throws ValueFormatException, RepositoryException {
        if (value instanceof Double) {
            return ((Double) value);
        }
        try {
            return Double.parseDouble(getString());
        } catch (NumberFormatException e) {
            throw new ValueFormatException(e);
        }
    }

    public BigDecimal getDecimal() throws ValueFormatException, RepositoryException {
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value);
        }
        return BigDecimal.valueOf(getDouble());
    }

    public Calendar getDate() throws ValueFormatException, RepositoryException {
        if (value instanceof Calendar) {
            return ((Calendar) value);
        }
        try {
            return ISO8601.parse(getString());
        } catch (RuntimeException e) {
            throw new ValueFormatException(e);
        }
    }

    public boolean getBoolean() throws ValueFormatException, RepositoryException {
        if (value instanceof Boolean) {
            return ((Boolean) value);
        }
        return Boolean.valueOf(getString());
    }

    public int getType() {
        return type;
    }
}
