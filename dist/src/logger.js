"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const winston_1 = require("winston");
const logFormat = winston_1.format.combine(winston_1.format.timestamp(), winston_1.format.printf(({ level, message, timestamp }) => `${timestamp} [${level}]: ${message}`));
const rootLogger = winston_1.createLogger({
    level: "debug",
    format: logFormat,
    defaultMeta: { target: "root" },
    transports: [new winston_1.transports.Console()]
});
exports.rootLogger = rootLogger;
