// Rules definition
const minRule = (minValue, errorMsg) => (value) =>
  value.length < minValue ? errorMsg : null;

const maxRule = (maxValue, errorMsg) => (value) =>
  value.length > maxValue ? errorMsg : null;

const stringRule = (errorMsg) => (value) =>
  typeof value !== "string" ? errorMsg : null;

const requiredRule = (errorMsg) => (value) =>
  !value || value.trim() === "" ? errorMsg : null;

const regexRule = (pattern, errorMsg) => (value) =>
  !pattern.test(value) ? errorMsg : null;

// We want to allow functions to be chained in the validator object to allow behaviours like:
// validator.string().min().require()
const createChain = (rules = []) => {
  const fn = (value) => {
    for (const rule of rules) {
      const error = rule(value);
      if (error) return error;
    }
    return null;
  };

  fn.min = (minValue, errorMsg) =>
    createChain([...rules, minRule(minValue, errorMsg)]);
  fn.max = (maxValue, errorMsg) =>
    createChain([...rules, maxRule(maxValue, errorMsg)]);
  fn.string = (errorMsg) => createChain([...rules, stringRule(errorMsg)]);
  fn.required = (errorMsg) => createChain([...rules, requiredRule(errorMsg)]);
  fn.regex = (pattern, errorMsg) =>
    createChain([...rules, regexRule(pattern, errorMsg)]);

  return fn;
};

const validator = createChain();

// We add the refinements to create custom rules that are not possible to be created
// as a predefined rule that compares the values of two inputs.
validator.schema = (shape) => {
  const schema = { shape, refinements: [] };

  schema.refine = (fn, { field, message }) => {
    schema.refinements.push({ fn, field, message });
    return schema;
  };

  return schema;
};

export default validator;
