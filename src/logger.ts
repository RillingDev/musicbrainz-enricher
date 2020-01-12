import { createLogger, format, transports } from "winston";

const logFormat = format.combine(
    format.timestamp(),
    format.printf(
        ({ level, message, timestamp }) => `${timestamp} [${level}]: ${message}`
    )
);

const rootLogger = createLogger({
    level: "info",
    format: logFormat,
    defaultMeta: { target: "root" },
    transports: [new transports.Console()]
});

export { rootLogger };
