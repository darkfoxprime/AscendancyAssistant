package com.github.jearls.ascendancyassistant;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * <p>The data model for the research list.  This stores the research
 * tree and current researching list in two tables:</p> <ul><li><p>The
 * <b>Research</b> table:</p> <table><tr><th>Column Name</th><th>Used
 * for</th></th><th>Type</th><th>Attributes</th><th>Default
 * Value</th></tr> <tr><th>ResearchProject</th><td>Holds the project
 * name</td><td>TEXT</td><td>UNIQUE NOT NULL</td><td></td></tr>
 * <tr><th>IsComplete</th><td>1 if the project has been completed, 0
 * otherwise</td><td>INTEGER</td><td></td>NOT NULL</td><td>0</td></tr>
 * <tr><th>PathNumber</th><td>The path index of the project, or NULL if
 * the project is not being worked on</td><td>INTEGER</td><td></td><td>NULL</td></tr>
 * <tr><th>GoalNumber</th><td>The goal index of the project, or NULL if
 * the project is not a goal</td><td>INTEGER</td><td></td><td>NULL</td></tr>
 * </table> </li> <li><p>The <b>ResearchDependencies</b> table:</p>
 * <table><tr><th>Column Name</th><th>Used for</th></th><th>Type</th><th>Attributes</th><th>Default
 * Value</th></tr> <tr><th>Depender</th><td>The ID of the research
 * project that depends on the Dependent.</td><td>INTEGER</td><td>NOT
 * NULL REFERENCES Research (column_id)</td><td></td></tr>
 * <tr><th>Dependent</th><td>The ID of the research project that is
 * depended on by the Depender.</td><td>INTEGER</td><td>NOT NULL
 * REFERENCES Research (column_id)</td><td></td></tr> </table> </li>
 * </ul>
 *
 * @author Johnson Earls
 */
public class ResearchProjectDbModel {
    public static final boolean  DEBUG                                 = true;
    /* the research database */
    public static final String   DATABASE_NAME                         = "ascendancyassistant_research.db";
    public static final int      DATABASE_VERSION                      = 1;
    /* the Research table */
    public static final String   TABLE_RESEARCH_NAME                   = "research";
    public static final String   TABLE_RESEARCH_COL_PROJECT_NAME       = "project";
    public static final String   TABLE_RESEARCH_COL_PROJECT_DEF        = "TEXT UNIQUE NOT NULL";
    public static final String   TABLE_RESEARCH_COL_ISCOMPLETE_NAME    = "isComplete";
    public static final String   TABLE_RESEARCH_COL_ISCOMPLETE_DEF     = "INTEGER NOT NULL DEFAULT 0";
    public static final String   TABLE_RESEARCH_COL_PATHNUMBER_NAME    = "pathNumber";
    public static final String   TABLE_RESEARCH_COL_PATHNUMBER_DEF     = "INTEGER DEFAULT NULL";
    public static final String   TABLE_RESEARCH_COL_GOALNUMBER_NAME    = "goalNumber";
    public static final String   TABLE_RESEARCH_COL_GOALNUMBER_DEF     = "INTEGER DEFAULT NULL";
    public static final String   TABLE_RESEARCH_CREATE                 = "CREATE TABLE " + TABLE_RESEARCH_NAME + " (" + TABLE_RESEARCH_COL_PROJECT_NAME + " " + TABLE_RESEARCH_COL_PROJECT_DEF + ", " + TABLE_RESEARCH_COL_ISCOMPLETE_NAME + " " + TABLE_RESEARCH_COL_ISCOMPLETE_DEF + ", " + TABLE_RESEARCH_COL_PATHNUMBER_NAME + " " + TABLE_RESEARCH_COL_PATHNUMBER_DEF + ", " + TABLE_RESEARCH_COL_GOALNUMBER_NAME + " " + TABLE_RESEARCH_COL_GOALNUMBER_DEF + ")";
    public static final String   TABLE_RESEARCH_DELETE                 = "DROP TABLE " + TABLE_RESEARCH_NAME + " IF EXISTS";
    public static final String[] TABLE_RESEARCH_UPDATE_VERSION         = {
            "" // no database version 0 to update from
    };
    /* the Dependency table */
    public static final String   TABLE_DEPENDENCIES_NAME               = "research_dependencies";
    public static final String   TABLE_DEPENDENCIES_COL_DEPENDER_NAME  = "depender";
    public static final String   TABLE_DEPENDENCIES_COL_DEPENDER_DEF   = "TEXT NOT NULL";
    public static final String   TABLE_DEPENDENCIES_COL_DEPENDER_KEY   = "FOREIGN KEY(" + TABLE_DEPENDENCIES_COL_DEPENDER_NAME + ") REFERENCES " + TABLE_RESEARCH_NAME + " (" + TABLE_RESEARCH_COL_PROJECT_NAME + ")";
    public static final String   TABLE_DEPENDENCIES_COL_DEPENDENT_NAME = "dependent";
    public static final String   TABLE_DEPENDENCIES_COL_DEPENDENT_DEF  = "INTEGER NOT NULL";
    public static final String   TABLE_DEPENDENCIES_COL_DEPENDENT_KEY  = "FOREIGN KEY(" + TABLE_DEPENDENCIES_COL_DEPENDER_NAME + ") REFERENCES " + TABLE_RESEARCH_NAME + " (" + TABLE_RESEARCH_COL_PROJECT_NAME + ")";
    public static final String   TABLE_DEPENDENCIES_CREATE             = "CREATE TABLE " + TABLE_DEPENDENCIES_NAME + " (" + TABLE_DEPENDENCIES_COL_DEPENDER_NAME + " " + TABLE_DEPENDENCIES_COL_DEPENDER_DEF + ", " + TABLE_DEPENDENCIES_COL_DEPENDENT_NAME + " " + TABLE_DEPENDENCIES_COL_DEPENDENT_DEF + ", " + TABLE_DEPENDENCIES_COL_DEPENDER_KEY + ", " + TABLE_DEPENDENCIES_COL_DEPENDENT_KEY + ")";
    public static final String   TABLE_DEPENDENCIES_DELETE             = "DROP TABLE " + TABLE_DEPENDENCIES_NAME + " IF EXISTS";
    public static final String[] TABLE_DEPENDENCIES_UPDATE_VERSION     = {
            "" // no database version 0 to update from
    };

    private final Context mContext;
    private ResearchProjectDbHelper mDbHelper = null;
    private SQLiteDatabase          mDb       = null;

    public ResearchProjectDbModel(Context context) {
        if (DEBUG) debug("> ResearchProjectDbModel(" + context + ")");
        if (DEBUG) debug("< ResearchProjectDbModel(" + context + ")");
        this.mContext = context;
    }

    public static void debug(String msg) {
        System.err.println("ResearchProjectDbModel: " + msg);
    }

    public void open() throws SQLException {
        if (DEBUG) debug("> open()");
        mDbHelper = new ResearchProjectDbHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        if (DEBUG) debug("< open()");
    }

    public void close() {
        if (DEBUG) debug("> close()");
        if (mDbHelper != null) {
            mDbHelper.close();
        }
        mDbHelper = null;
        mDb = null;
        if (DEBUG) debug("< close()");
    }

    private void createResearchProject(String projectName, boolean isComplete, Integer pathNumber, Integer goalNumber) {
        if (DEBUG)
            debug("> createResearchProject(" + projectName + "," + isComplete + "," + pathNumber + "," + goalNumber + ")");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_RESEARCH_COL_PROJECT_NAME, projectName);
        contentValues.put(TABLE_RESEARCH_COL_ISCOMPLETE_NAME, isComplete ? 1 : 0);
        contentValues.put(TABLE_RESEARCH_COL_PATHNUMBER_NAME, pathNumber);
        contentValues.put(TABLE_RESEARCH_COL_GOALNUMBER_NAME, goalNumber);
        mDb.insert(TABLE_RESEARCH_NAME, null, contentValues);
        if (DEBUG)
            debug("< createResearchProject(" + projectName + "," + isComplete + "," + pathNumber + "," + goalNumber + ")");
    }

    private void createResearchProject(ResearchProject project) {
        if (DEBUG) debug("> createResearchProject(" + project + ")");
        createResearchProject(project.getName(), project.isCompleted(), project.isInPath() ? project.getPathNumber() : null, project.isGoal() ? project.getGoalNumber() : null);
        if (DEBUG) debug("< createResearchProject(" + project + ")");
    }

    private void createResearchDependency(String depender, String dependent) {
        if (DEBUG)
            debug("> createResearchDependency(" + depender + ", " + dependent + ")");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_DEPENDENCIES_COL_DEPENDER_NAME, depender);
        contentValues.put(TABLE_DEPENDENCIES_COL_DEPENDENT_NAME, dependent);
        mDb.insert(TABLE_DEPENDENCIES_NAME, null, contentValues);
        if (DEBUG)
            debug("< createResearchDependency(" + depender + ", " + dependent + ")");
    }

    private void createResearchDependency(ResearchProject depender, ResearchProject dependent) {
        if (DEBUG)
            debug("> createResearchDependency(" + depender + ", " + dependent + ")");
        createResearchDependency(depender.getName(), dependent.getName());
        if (DEBUG)
            debug("< createResearchDependency(" + depender + ", " + dependent + ")");
    }

    public void createResearchTree() {
        if (DEBUG) debug("> createResearchTree()");
        mDb.beginTransaction();
        for (ResearchProject researchProject : ResearchProject.getAllResearchProjects()) {
            createResearchProject(researchProject);
        }
        for (ResearchProject researchProject : ResearchProject.getAllResearchProjects()) {
            for (ResearchProject dependentResearchProject : researchProject.getDependents()) {
                createResearchDependency(researchProject, dependentResearchProject);
            }
        }
        mDb.setTransactionSuccessful();
        mDb.endTransaction();
        if (DEBUG) debug("> createResearchTree()");
    }

    private Cursor fetchAllResearchProjects(String orderBy) {
        if (DEBUG) debug("> fetchAllResearchProjects(" + orderBy + ")");
        Cursor cursor = mDb.query(TABLE_RESEARCH_NAME, new String[]{TABLE_RESEARCH_COL_PROJECT_NAME, TABLE_RESEARCH_COL_ISCOMPLETE_NAME, TABLE_RESEARCH_COL_PATHNUMBER_NAME, TABLE_RESEARCH_COL_GOALNUMBER_NAME}, null, null, null, null, orderBy);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        if (DEBUG)
            debug("< fetchAllResearchProjects(" + orderBy + ") returning " + cursor);
        return cursor;
    }

    private Cursor fetchAllResearchProjects() {
        return fetchAllResearchProjects(null);
    }

    private Cursor fetchAllResearchDependencies(String orderBy) {
        if (DEBUG)
            debug("> fetchAllResearchDependencies(" + orderBy + ")");
        Cursor cursor = mDb.query(TABLE_DEPENDENCIES_NAME, new String[]{TABLE_DEPENDENCIES_COL_DEPENDER_NAME, TABLE_DEPENDENCIES_COL_DEPENDENT_NAME}, null, null, null, null, orderBy);
        if (cursor != null) {
            cursor.moveToFirst();
        }
        if (DEBUG)
            debug("< fetchAllResearchDependencies(" + orderBy + ") returning " + cursor);
        return cursor;
    }

    private Cursor fetchAllResearchDependencies() {
        return fetchAllResearchDependencies(null);
    }

    public void fetchResearchTree() {
        if (DEBUG) debug("> fetchResearchTree()");
        // turn on batching of triggers
        ResearchProject.enterBatchTriggerMode();
        // clear the existing research tree
        ResearchProject.clearResearchProjects();
        // fetch all research projects, in order of Path Number
        Cursor cursor = fetchAllResearchProjects(TABLE_RESEARCH_COL_PATHNUMBER_NAME);
        if (cursor != null) {
            while (!cursor.isAfterLast()) {
                if (DEBUG)
                    debug("* fetchResearchTree: research project cursor = " + cursor.toString());
                ResearchProject project = new ResearchProject(cursor.getString(cursor.getColumnIndex(TABLE_RESEARCH_COL_PROJECT_NAME)));
                project.setCompleted(cursor.getInt(cursor.getColumnIndex(TABLE_RESEARCH_COL_ISCOMPLETE_NAME)) != 0);
                if (!cursor.isNull(cursor.getColumnIndex(TABLE_RESEARCH_COL_PATHNUMBER_NAME))) {
                    project.addToPath();
                    if (!cursor.isNull(cursor.getColumnIndex(TABLE_RESEARCH_COL_GOALNUMBER_NAME))) {
                        project.addToGoals();
                    }
                }
                cursor.moveToNext();
            }
        }
        // fetch all dependencies and fill them in
        cursor = fetchAllResearchDependencies();
        if (cursor != null) {
            while (!cursor.isAfterLast()) {
                if (DEBUG)
                    debug("* fetchResearchTree: research dependency cursor = " + cursor.toString());
                ResearchProject depender  = ResearchProject.getResearchProject(cursor.getString(cursor.getColumnIndex(TABLE_DEPENDENCIES_COL_DEPENDER_NAME)));
                ResearchProject dependent = ResearchProject.getResearchProject(cursor.getString(cursor.getColumnIndex(TABLE_DEPENDENCIES_COL_DEPENDENT_NAME)));
                depender.addDependent(dependent);
                cursor.moveToNext();
            }
        }
        // run all batched triggers
        ResearchProject.leaveBatchTriggerMode();
        if (DEBUG) debug("< fetchResearchTree()");
    }

    private void updateResearchProject(String projectName, boolean isComplete, Integer pathNumber, Integer goalNumber) {
        if (DEBUG)
            debug("> updateResearchProject(" + projectName + ", " + isComplete + ", " + pathNumber + ", " + goalNumber + ")");
        ContentValues contentValues = new ContentValues();
        contentValues.put(TABLE_RESEARCH_COL_ISCOMPLETE_NAME, isComplete ? 1 : 0);
        contentValues.put(TABLE_RESEARCH_COL_PATHNUMBER_NAME, pathNumber);
        contentValues.put(TABLE_RESEARCH_COL_GOALNUMBER_NAME, goalNumber);
        mDb.update(TABLE_RESEARCH_NAME, contentValues, TABLE_RESEARCH_COL_PROJECT_NAME + "=?", new String[]{projectName});
        if (DEBUG)
            debug("< updateResearchProject(" + projectName + ", " + isComplete + ", " + pathNumber + ", " + goalNumber + ")");
    }

    public void updateResearchProject(ResearchProject project) {
        if (DEBUG) debug("> updateResearchProject(" + project + ")");
        updateResearchProject(project.getName(), project.isCompleted(), project.isInPath() ? project.getPathNumber() : null, project.isGoal() ? project.getGoalNumber() : null);
        if (DEBUG) debug("< updateResearchProject(" + project + ")");
    }

    private static class ResearchProjectDbHelper extends SQLiteOpenHelper {
        public ResearchProjectDbHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            if (DEBUG)
                debug("> ResearchProjectDbHelper.onCreate(" + db + ")");
            if (DEBUG)
                debug("* ResearchProjectDbHelper.onCreate: " + TABLE_RESEARCH_CREATE);
            db.execSQL(TABLE_RESEARCH_CREATE);
            if (DEBUG)
                debug("* ResearchProjectDbHelper.onCreate: " + TABLE_DEPENDENCIES_CREATE);
            db.execSQL(TABLE_DEPENDENCIES_CREATE);
            if (DEBUG)
                debug("< ResearchProjectDbHelper.onCreate(" + db + ")");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int fromVersion, int toVersion) {
            if (DEBUG)
                debug("> ResearchProjectDbHelper.onUpgrade(" + db + ", " + fromVersion + ", " + toVersion + ")");
            // execute each non-null UPDATE_VERSION string in turn starting
            // at fromVersion and stopping just before toVersion
            for (int i = fromVersion; i < toVersion; i += 1) {
                if (TABLE_RESEARCH_UPDATE_VERSION[i] != null) {
                    if (DEBUG)
                        debug("* ResearchProjectDbHelper.onUpgrade: " + TABLE_RESEARCH_UPDATE_VERSION[i]);
                    db.execSQL(TABLE_RESEARCH_UPDATE_VERSION[i]);
                }
                if (TABLE_DEPENDENCIES_UPDATE_VERSION[i] != null) {
                    if (DEBUG)
                        debug("* ResearchProjectDbHelper.onUpgrade: " + TABLE_DEPENDENCIES_UPDATE_VERSION[i]);
                    db.execSQL(TABLE_DEPENDENCIES_UPDATE_VERSION[i]);
                }
            }
            if (DEBUG)
                debug("< ResearchProjectDbHelper.onUpgrade(" + db + ", " + fromVersion + ", " + toVersion + ")");
        }
    }
}
