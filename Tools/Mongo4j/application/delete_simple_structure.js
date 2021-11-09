const mongoose = require('mongoose');
const mongo4j = require('mongo4j');
const Student = require('./models/student_simple_structure');

main().catch(err => console.log(err));

async function main() {
    // connect to 'test' database
    const mongoUri = process.env.MONGO_URI || "mongodb://admin:admin@localhost:27017/test?authSource=admin";
    const neo4jUri = process.env.NEO_URI || "bolt://localhost:7687/";
    const neo4jUser = process.env.NEO_USER || "neo4j";
    const neo4jPass = process.env.NEO_PASS || "admin";

    await mongoose.connect(mongoUri);
    // initialize mongo4j with drivers
    mongo4j.init(neo4jUri, {user: neo4jUser, pass: neo4jPass});

    Student.findOne({ name: "John" }).then((result) => {
        console.log("found document:");
        console.log(result);
        result.remove().then((result) => {
            console.log("document deleted");
        });
    });
}