import { reactive, watch } from "vue";

export function useForm({ validationSchema }) {
  const fields = reactive(
    Object.fromEntries(
      Object.keys(validationSchema.shape).map((key) => [key, ""]),
    ),
  );
  const errors = reactive(
    Object.fromEntries(
      Object.keys(validationSchema.shape).map((key) => [key, null]),
    ),
  );

  for (const key in validationSchema.shape) {
    watch(
      () => fields[key],
      () => {
        errors[key] = validationSchema.shape[key](fields[key]);
        for (const { fn, field, message } of validationSchema.refinements) {
          if (!fn(fields)) errors[field] = message;
          else errors[field] = null;
        }
      },
    );
  }

  function validate() {
    for (const key in validationSchema.shape) {
      errors[key] = validationSchema.shape[key](fields[key]);
    }
    for (const { fn, field, message } of validationSchema.refinements) {
      if (!fn(fields)) errors[field] = message;
      else errors[field] = null;
    }
  }

  const handleSubmit = (method) => async (event) => {
    event?.preventDefault();

    // validate using validationSchema
    validate();

    const hasErrors = Object.values(errors).some((e) => e !== null);
    if (hasErrors) return;

    // call method
    await method(fields);
  };

  return { fields, errors, handleSubmit };
}
