/*

	This file is part of GALE (Generic Adaptation Language and Engine).

    GALE is free software: you can redistribute it and/or modify it under the 
    terms of the GNU Lesser General Public License as published by the Free 
    Software Foundation, either version 3 of the License, or (at your option) 
    any later version.

    GALE is distributed in the hope that it will be useful, but WITHOUT ANY 
    WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
    FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for 
    more details.

    You should have received a copy of the GNU Lesser General Public License
    along with GALE. If not, see <http://www.gnu.org/licenses/>.
    
 */
/**
 * DataManager.java
 * Last modified: $Date$
 * In revision:   $Revision$
 * Modified by:   $Author$
 *
 * Copyright (c) 2008-2011 Eindhoven University of Technology.
 * All Rights Reserved.
 *
 * This software is proprietary information of the Eindhoven University
 * of Technology. It may be used according to the GNU LGPL license.
 */
package nl.tue.gale.ae;

import nl.tue.gale.common.GaleUtil;
import nl.tue.gale.um.data.EntityValue;
import nl.tue.gale.um.data.UserEntity;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

public class DataManager extends HibernateDaoSupport {
	public void init() {
		UserEntity ue = (UserEntity) getHibernateTemplate().get(
				UserEntity.class, "admin");
		if (ue == null) {
			EntityValue ev;

			ue = new UserEntity("author");
			ue.setProperty("author", "true");
			ue.setProperty("password", GaleUtil.digest("aha"));
			ue.setProperty("courses", "courses:001:tutorial");
			getHibernateTemplate().save(ue);
			ev = new EntityValue("gale://author@gale.tue.nl/personal#email");
			ev.setValue("dsmits@win.tue.nl");
			getHibernateTemplate().save(ev);
			ev = new EntityValue("gale://author@gale.tue.nl/personal#name");
			ev.setValue("Gale Author");
			getHibernateTemplate().save(ev);
			ev = new EntityValue("gale://author@gale.tue.nl/personal#os");
			ev.setValue("not set");
			getHibernateTemplate().save(ev);
			ev = new EntityValue("gale://author@gale.tue.nl/personal#system");
			ev.setValue("not set");
			getHibernateTemplate().save(ev);

			ue = new UserEntity("admin");
			ue.setProperty("author", "true");
			ue.setProperty("admin", "true");
			ue.setProperty("password", GaleUtil.digest("admin"));
			getHibernateTemplate().save(ue);
			ev = new EntityValue("gale://admin@gale.tue.nl/personal#email");
			ev.setValue("debra@win.tue.nl");
			getHibernateTemplate().save(ev);
			ev = new EntityValue("gale://admin@gale.tue.nl/personal#name");
			ev.setValue("Gale Administrator");
			getHibernateTemplate().save(ev);
		}
	}
}
