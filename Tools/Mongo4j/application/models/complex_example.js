const mongo4j = require('mongo4j');
const mongoose = require('mongoose');
const Schema = mongoose.Schema;

const AddressSchema = new Schema({
    city: {
        type: String,
        neo_prop: true
    },
    street: {
        type: String,
        neo_prop: true
    }
});

const StudentSchema = new Schema({
    firstname: {
        type: String,
        neo_prop: true
    },
    lastname: {
        type: String,
        neo_prop: true
    },
    address: AddressSchema,
    marks: [{
        type: mongoose.Types.Decimal128,
        neo_prop: true
    }],
    pass: {
        type: Boolean,
        neo_prop: true
    },
});

const TeacherSchema = new Schema({
    firstname: {
        type: String,
        neo_prop: true
    },
    lastname: {
        type: String,
        neo_prop: true
    }
});

const CourseSchema = new Schema({
    name: {
        type: String,
        neo_prop: true
    },
    teacher: TeacherSchema,
    students: [StudentSchema]
});

CourseSchema.plugin(mongo4j.plugin());

const Course = mongoose.model('course', CourseSchema);

module.exports = Course;