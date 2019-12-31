const isProductionMode = (): boolean => process.env.NODE_ENV === "production";

export { isProductionMode };
