(() => {
  const filters = document.querySelector('#career-filters');
  const jobs = [...document.querySelectorAll('[data-career-job]')];
  if (!filters || jobs.length === 0) return;

  const fields = {
    search: document.querySelector('#career-search'),
    department: document.querySelector('#career-department'),
    location: document.querySelector('#career-location'),
    workMode: document.querySelector('#career-work-mode'),
    employmentType: document.querySelector('#career-employment-type')
  };
  const noResults = document.querySelector('[data-career-no-results]');
  const count = document.querySelector('[data-career-count]');
  const normalize = value => (value || '').trim().toLocaleLowerCase();

  const applyFilters = () => {
    const query = normalize(fields.search?.value);
    const department = normalize(fields.department?.value);
    const location = normalize(fields.location?.value);
    const workMode = normalize(fields.workMode?.value);
    const employmentType = normalize(fields.employmentType?.value);
    let visible = 0;

    jobs.forEach(job => {
      const searchable = normalize(`${job.dataset.title} ${job.dataset.keywords} ${job.textContent}`);
      const matches = (!query || searchable.includes(query))
        && (!department || normalize(job.dataset.department) === department)
        && (!location || normalize(job.dataset.location).includes(location))
        && (!workMode || normalize(job.dataset.workMode) === workMode)
        && (!employmentType || normalize(job.dataset.employmentType) === employmentType);
      job.hidden = !matches;
      if (matches) visible += 1;
    });

    if (noResults) noResults.hidden = visible !== 0;
    if (count) count.textContent = `Showing ${visible} of ${jobs.length} open position${jobs.length === 1 ? '' : 's'}`;
  };

  Object.values(fields).forEach(field => field?.addEventListener('input', applyFilters));
  Object.values(fields).forEach(field => field?.addEventListener('change', applyFilters));
  document.querySelector('[data-career-clear]')?.addEventListener('click', () => {
    Object.values(fields).forEach(field => { if (field) field.value = ''; });
    applyFilters();
    fields.search?.focus();
  });
  applyFilters();
})();
