const mongo4j = require('mongo4j');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const StudentSchema = new Schema({
    name: {
        type: String,
        neo_prop: true
    },
    age: {
        type: Number,
        neo_prop: true
    },
    dateOfBirth: {
        type: String,
        neo_prop: true
    },
    score: {
        type: mongoose.Types.Decimal128,
        neo_prop: true
    },
    pass: {
        type: Boolean,
        neo_prop: true
    }
});

StudentSchema.plugin(mongo4j.plugin());

const Student = mongoose.model('student', StudentSchema);

module.exports = Student;