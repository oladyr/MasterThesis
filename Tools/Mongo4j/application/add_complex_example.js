const mongoose = require('mongoose');
const mongo4j = require('mongo4j');
const Course = require('./models/complex_example');

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

    const course1 = new Course({
        name: "Biology",
        teacher: { firstname: "John",  lastname: "Adams"},
        students: [
            {
                firstname: "Amy",
                lastname: "Smith",
                address: {
                    city: "New York",
                    street: "7381 Loop St."
                },
                marks: [5,5,5],
                pass: true
            },
            {
                firstname: "James",
                lastname: "Lawrence",
                address: {
                    city: "Los Angeles",
                    street: "8410 Locust St."
                },
                marks: [3.5,4,5],
                pass: true
            }
        ]
    });

    const course2 = new Course({
        name: "Chemistry",
        teacher: { firstname: "Caroline",  lastname: "Yellow"},
        students: [
            {
                firstname: "Chris",
                lastname: "Red",
                address: {
                    city: "Warsaw",
                    street: "Niska 3"
                },
                marks: [5,5,5],
                pass: true
            },
            {
                firstname: "Sam",
                lastname: "King",
                address: {
                    city: "Gliwice",
                    street: "Wysoka 2"
                },
                marks: [3.5,4,5],
                pass: true
            }
        ]
    });

    Course.insertMany([course1, course2]).then((result) => {
        console.log("documents saved");
    });
}