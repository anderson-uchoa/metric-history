package org.metrichistory.storage;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.metrichistory.model.MeasureStore;
import org.metrichistory.model.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

/**
 * Represents a connection to a storage instance of mongodb. Builds with {@link DatabaseBuilder}.
 */
public class MongoAdapter implements Database{

    private static final Logger logger = LoggerFactory.getLogger(MongoAdapter.class);

    private static final String COLLECTION_REVISION = "revisions";
    private static final String COLLECTION_CLASS = "classes";
    private static final String FIELD_METRICS = "metrics";
    private static final String FIELD_DIFF = "fluctuations";
    private static final String FIELD_REVISION = "revision";
    private static final String FIELD_CLASS_NAME = "name";
    private static final String INDEX_NAME = "compound_revision_name_1";

    private final MongoDatabase database;

    /**
     * Create a new connection to the storage.
     * @param database the storage
     */
    MongoAdapter(MongoDatabase database) {
        this.database = database;
    }

    @Override
    public void persist(HashMap<String, String> ancestry) {
        final MongoCollection<Document> revisions = database.getCollection(COLLECTION_REVISION);
        final List<Document> documents = createDocuments(ancestry);
        revisions.insertMany(documents);
    }

    @Override
    public void setRaw(MeasureStore measureStore) {
        setsClassMeasurement(measureStore, FIELD_METRICS);
    }

    @Override
    public void setDiff(MeasureStore data) {
        setsClassMeasurement(data, FIELD_DIFF);
    }

    private void setsClassMeasurement(MeasureStore data, String measurementName) {
        logger.info("Loading collection...");
        final MongoCollection<Document> collection = database.getCollection(COLLECTION_CLASS);

        logger.info("Verifying indexes...");
        verifyIndexes(collection);

        logger.info("Exporting class measurements...");

        final List<Document> pendingDocuments = new ArrayList<>();

        for (String revision : data.versions()) {
            for (String className : data.artifacts(revision)) {
                final Bson documentFilter = Filters.and(eq(FIELD_REVISION, revision), eq(FIELD_CLASS_NAME, className));

                Document document = collection.find(documentFilter).first();
                if (document == null) {
                    document = createDocument(revision, className, data.get(revision, className),
                            measurementName);
                    pendingDocuments.add(document);
                } else {
                    collection.updateOne(document, new Document("$set",
                            new Document(measurementName, createDocument(data.get(revision, className)))));
                }
            }
        }

        if (!pendingDocuments.isEmpty()) {
            collection.insertMany(pendingDocuments);
        }

        logger.info("Exportation finished");
    }

    private void verifyIndexes(MongoCollection<Document> collection) {
        for (Document document : collection.listIndexes()) {
            if (document.containsKey("name") && document.getString("name").equalsIgnoreCase(INDEX_NAME)) {
                return;
            }
        }

        collection.createIndex(Indexes.ascending(FIELD_REVISION, FIELD_CLASS_NAME),
                new IndexOptions().name(INDEX_NAME));
        logger.info("-> Created missing index '{}'", INDEX_NAME);
    }

    private Document createDocument(String revision, String className, Metrics metrics, String measurementName) {
        final Document result = new Document();
        result.append(FIELD_REVISION, revision);
        result.append(FIELD_CLASS_NAME, className);
        result.append(measurementName, createDocument(metrics));
        return result;
    }

    private Document createDocument(Metrics metric) {
        final Map<String, Double> metrics = Stores.convertToSourceMeterFormat(metric);

        final Document result = new Document();
        metrics.forEach(result::append);
        return result;
    }

    private List<Document> createDocuments(HashMap<String, String> ancestry) {
        final ArrayList<Document> documents = new ArrayList<>(ancestry.size());
        ancestry.forEach((s, s2) -> documents.add(new Document(FIELD_REVISION, s).append("parent", s2)));
        return documents;
    }
}
