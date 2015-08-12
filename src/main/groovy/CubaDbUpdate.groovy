/*
 * Copyright (c) 2008-2013 Haulmont. All rights reserved.
 * Use is subject to license terms, see http://www.cuba-platform.com/license for details.
 */

import groovy.sql.Sql
import org.gradle.api.tasks.TaskAction

import java.sql.SQLException
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author krivopustov
 * @version $Id$
 */
class CubaDbUpdate extends CubaDbTask {

    def requiredTables = ['reports': 'report_report',
                          'workflow': 'wf_proc',
                          'ccpayments': 'cc_credit_card',
                          'bpm': 'bpm_proc_definition']

    CubaDbUpdate() {
        setGroup('Database')
    }

    @TaskAction
    def updateDb() {
        init()

        try {
            runRequiredInitScripts()

            ScriptFinder scriptFinder = new ScriptFinder(dbms, dbmsVersion, dbDir, ['sql'])
            List<File> files = scriptFinder.getUpdateScripts()

            List<String> scripts = getExecutedScripts()
            def toExecute = files.findAll { File file ->
                String name = getScriptName(file)
                !scripts.contains(name)
            }

            if (project.logger.isInfoEnabled()) {
                project.logger.info("Updates: \n" + toExecute.collect{ "\t" + it.getAbsolutePath() }.join("\n") )
            }

            toExecute.each { File file ->
                executeScript(file)
                String name = getScriptName(file)
                markScript(name, false)
            }
        } finally {
            closeSql()
        }
    }

    protected void runRequiredInitScripts() {
        ScriptFinder scriptFinder = new ScriptFinder(dbms, dbmsVersion, dbDir, ['sql'])
        scriptFinder.getModuleDirs().each { String dirName ->
            def moduleName = dirName.substring(3)
            def reqTable = requiredTables[moduleName]
            if (reqTable) {
                try {
                    def sqlLogger = Logger.getLogger(Sql.class.getName())
                    def saveLevel = sqlLogger.level
                    try {
                        sqlLogger.level = Level.SEVERE // suppress confusing error output
                        getSql().rows("select * from $reqTable where 0=1".toString())
                    } finally {
                        sqlLogger.level = saveLevel
                    }
                } catch (SQLException e) {
                    String mark = dbms == 'oracle' ? 'table or view does not exist' : reqTable
                    if (e.message?.toLowerCase()?.contains(mark)) {
                        project.logger.warn("Required table for $moduleName does not exist, running init scripts")
                        // probably the required table does not exist
                        initDatabase(dirName)
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

    protected void executeScript(File file) {
        project.logger.warn("Executing script " + file.getPath())
        if (file.name.endsWith('.sql'))
            executeSqlScript(file)
    }


    protected List<String> getExecutedScripts() {
        return getSql().rows('select SCRIPT_NAME from SYS_DB_CHANGELOG').collect { row -> row.script_name }
    }
}