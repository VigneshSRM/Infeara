(() => {
  const mobileButton = document.querySelector('.mobile-menu-toggle');
  const mobileMenu = document.querySelector('#mobile-menu');
  const dropdowns = [
    { wrap: '.nav-services-wrap', button: '.services-toggle', menu: '#services-menu' },
    { wrap: '.nav-industries-wrap', button: '.industries-toggle', menu: '#industries-menu' },
    { wrap: '.nav-about-wrap', button: '.about-toggle', menu: '#about-menu' }
  ].map((item) => ({ ...item, button: document.querySelector(item.button), menu: document.querySelector(item.menu) }));

  const closeDropdown = (dropdown) => {
    if (!dropdown.button || !dropdown.menu) return;
    dropdown.button.setAttribute('aria-expanded', 'false');
    dropdown.menu.hidden = true;
  };
  const closeDesktopDropdowns = (except = null) => dropdowns.forEach((item) => {
    if (item !== except) closeDropdown(item);
  });
  const closeMobileSubmenus = () => {
    document.querySelectorAll('[data-mobile-submenu-toggle]').forEach((button) => {
      button.setAttribute('aria-expanded', 'false');
      const panel = document.getElementById(button.getAttribute('aria-controls'));
      if (panel) {
        panel.hidden = true;
        panel.querySelectorAll('details[open]').forEach((details) => { details.open = false; });
      }
    });
  };

  dropdowns.forEach((dropdown) => dropdown.button?.addEventListener('click', () => {
    const opening = dropdown.button.getAttribute('aria-expanded') !== 'true';
    closeDesktopDropdowns(dropdown);
    if (opening) {
      closeMobileSubmenus();
      dropdown.button.setAttribute('aria-expanded', 'true');
      dropdown.menu.hidden = false;
    } else {
      closeDropdown(dropdown);
    }
  }));
  document.querySelectorAll('[data-mobile-submenu-toggle]').forEach((button) => {
    button.addEventListener('click', () => {
      const opening = button.getAttribute('aria-expanded') !== 'true';
      closeMobileSubmenus();
      if (opening) {
        const panel = document.getElementById(button.getAttribute('aria-controls'));
        button.setAttribute('aria-expanded', 'true');
        if (panel) panel.hidden = false;
      }
    });
  });

  document.addEventListener('click', (event) => {
    dropdowns.forEach((dropdown) => {
      if (!event.target.closest(dropdown.wrap)) closeDropdown(dropdown);
    });
  });
  document.addEventListener('keydown', (event) => {
    if (event.key !== 'Escape') return;
    const openDetails = mobileMenu?.querySelectorAll('details[open]');
    if (openDetails?.length) {
      openDetails.forEach((details) => { details.open = false; });
      event.preventDefault();
      return;
    }
    const openDropdown = dropdowns.find((item) => item.button?.getAttribute('aria-expanded') === 'true');
    const mobileWasOpen = mobileButton?.getAttribute('aria-expanded') === 'true';
    closeDesktopDropdowns();
    closeMobileSubmenus();
    if (mobileWasOpen) {
      mobileButton.setAttribute('aria-expanded', 'false');
      mobileButton.setAttribute('aria-label', 'Open navigation');
      mobileMenu.hidden = true;
      mobileButton.focus();
    } else if (openDropdown) {
      openDropdown.button.focus();
    }
  });

  mobileButton?.addEventListener('click', () => {
    const opening = mobileButton.getAttribute('aria-expanded') !== 'true';
    mobileButton.setAttribute('aria-expanded', String(opening));
    mobileButton.setAttribute('aria-label', opening ? 'Close navigation' : 'Open navigation');
    mobileMenu.hidden = !opening;
    closeDesktopDropdowns();
    closeMobileSubmenus();
  });
  mobileMenu?.querySelectorAll('a').forEach((link) => {
    link.addEventListener('click', () => {
      mobileButton.setAttribute('aria-expanded', 'false');
      mobileButton.setAttribute('aria-label', 'Open navigation');
      mobileMenu.hidden = true;
      closeMobileSubmenus();
    });
  });

  const yearLabel = document.querySelector('#current-year');
  if (yearLabel) yearLabel.textContent = new Date().getFullYear();

  const form = document.querySelector('#consultation-form');
  const status = document.querySelector('#form-status');
  const submitButton = document.querySelector('#submit-button');
  const serviceSelect = form?.elements.serviceOfInterest;
  const serviceChoices = [...(serviceSelect?.options || [])].map((option) => option.value).filter(Boolean);

  const serviceForLink = (value) => {
    const text = (value || '').toLowerCase();
    if (text.includes('cyber') || text.includes('security')) return 'Cybersecurity services';
    if (text.includes('infrastructure') || text.includes('reliability')) return 'IT infrastructure services';
    if (text.includes('managed it')) return 'Managed IT services';
    if (text.includes('outsourc') || text.includes('remote support')) return 'IT outsourcing and remote support';
    if (text.includes('cloud') || text.includes('datacenter')) return 'Cloud and datacenter services';
    if (text.includes('network')) return 'Network engineering';
    if (text.includes('facility')) return 'IT facility management';
    if (text.includes('turnkey')) return 'Turnkey IT infrastructure projects';
    if (text.includes('automation') || text.includes('software') || text.includes('workflow')) return 'Automation and custom software';
    if (text.includes('modernize')) return 'IT infrastructure services';
    return 'Something else';
  };

  const requestedService = new URLSearchParams(window.location.search).get('service');
  if (requestedService && serviceSelect) {
    serviceSelect.value = serviceChoices.includes(requestedService) ? requestedService : serviceForLink(requestedService);
  }

  document.querySelectorAll('[data-service]').forEach((link) => {
    link.addEventListener('click', () => {
      if (serviceSelect) {
        const selected = serviceForLink(link.dataset.service);
        serviceSelect.value = serviceChoices.includes(selected) ? selected : 'Something else';
      }
    });
  });

  document.querySelectorAll('[data-photo-carousel]').forEach((carousel) => {
    const slides = [...carousel.querySelectorAll('[data-photo-slide]')];
    const dots = [...carousel.querySelectorAll('[data-photo-dot]')];
    const previous = carousel.querySelector('[data-photo-prev]');
    const next = carousel.querySelector('[data-photo-next]');
    const count = carousel.querySelector('[data-photo-count]');
    if (slides.length < 2) return;

    let current = 0;
    const showSlide = (index) => {
      current = (index + slides.length) % slides.length;
      slides.forEach((slide, position) => {
        const active = position === current;
        slide.hidden = !active;
        slide.setAttribute('aria-hidden', String(!active));
      });
      dots.forEach((dot, position) => {
        const active = position === current;
        dot.classList.toggle('is-active', active);
        dot.setAttribute('aria-current', String(active));
      });
      if (count) count.textContent = `${String(current + 1).padStart(2, '0')} / ${String(slides.length).padStart(2, '0')}`;
    };

    previous?.addEventListener('click', () => showSlide(current - 1));
    next?.addEventListener('click', () => showSlide(current + 1));
    dots.forEach((dot, index) => dot.addEventListener('click', () => showSlide(index)));
    carousel.addEventListener('keydown', (event) => {
      if (event.key === 'ArrowLeft') { event.preventDefault(); showSlide(current - 1); }
      if (event.key === 'ArrowRight') { event.preventDefault(); showSlide(current + 1); }
    });
    showSlide(0);
  });

  document.querySelectorAll('[data-specialist-carousel]').forEach((carousel) => {
    const slides = [...carousel.querySelectorAll('[data-specialist-slide]')];
    const dots = [...carousel.querySelectorAll('[data-specialist-dot]')];
    const syncKey = carousel.dataset.specialistCarousel;
    const linkedPhotoCarousel = [...document.querySelectorAll('[data-specialist-photo-carousel]')]
      .find((photoCarousel) => photoCarousel.dataset.specialistPhotoCarousel === syncKey);
    const linkedPhotoSlides = linkedPhotoCarousel ? [...linkedPhotoCarousel.querySelectorAll('[data-specialist-photo-slide]')] : [];
    const previous = carousel.querySelector('[data-specialist-prev]');
    const next = carousel.querySelector('[data-specialist-next]');
    const toggle = carousel.querySelector('[data-specialist-toggle]');
    const count = carousel.querySelector('[data-specialist-count]');
    if (slides.length < 2) return;

    const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    const interval = 6500;
    let current = 0;
    let playing = !reducedMotion;
    let hoverPaused = false;
    let focusPaused = false;
    let timer;

    const clearTimer = () => window.clearTimeout(timer);
    const canAdvanceAutomatically = () => playing && !hoverPaused && !focusPaused && document.visibilityState === 'visible';
    const schedule = () => {
      clearTimer();
      if (canAdvanceAutomatically()) timer = window.setTimeout(() => { showSlide(current + 1); schedule(); }, interval);
    };
    const updateToggle = () => {
      if (!toggle) return;
      toggle.textContent = playing ? 'Ⅱ' : '▶';
      toggle.setAttribute('aria-label', playing ? 'Pause service carousel' : 'Play service carousel');
      toggle.setAttribute('aria-pressed', String(!playing));
    };
    const showSlide = (index) => {
      current = (index + slides.length) % slides.length;
      slides.forEach((slide, position) => {
        const active = position === current;
        slide.hidden = !active;
        slide.classList.toggle('is-active', active);
        slide.setAttribute('aria-hidden', String(!active));
      });
      linkedPhotoSlides.forEach((slide, position) => {
        const active = position === current;
        slide.hidden = !active;
        slide.classList.toggle('is-active', active);
        slide.setAttribute('aria-hidden', String(!active));
      });
      dots.forEach((dot, position) => {
        const active = position === current;
        dot.classList.toggle('is-active', active);
        dot.setAttribute('aria-current', String(active));
      });
      if (count) count.textContent = `${String(current + 1).padStart(2, '0')} / ${String(slides.length).padStart(2, '0')}`;
    };

    previous?.addEventListener('click', () => { showSlide(current - 1); schedule(); });
    next?.addEventListener('click', () => { showSlide(current + 1); schedule(); });
    dots.forEach((dot, index) => dot.addEventListener('click', () => { showSlide(index); schedule(); }));
    toggle?.addEventListener('click', () => {
      playing = !playing;
      if (playing) { hoverPaused = false; focusPaused = false; }
      updateToggle();
      schedule();
    });
    const hoverTargets = linkedPhotoCarousel ? [carousel, linkedPhotoCarousel] : [carousel];
    const hoveredTargets = new Set();
    hoverTargets.forEach((target) => {
      target.addEventListener('pointerenter', () => {
        hoveredTargets.add(target);
        hoverPaused = true;
        clearTimer();
      });
      target.addEventListener('pointerleave', () => {
        hoveredTargets.delete(target);
        hoverPaused = hoveredTargets.size > 0;
        schedule();
      });
    });
    carousel.addEventListener('focusin', () => { focusPaused = true; clearTimer(); });
    carousel.addEventListener('focusout', () => requestAnimationFrame(() => {
      focusPaused = carousel.contains(document.activeElement);
      schedule();
    }));
    carousel.addEventListener('keydown', (event) => {
      if (event.key === 'ArrowLeft') { event.preventDefault(); showSlide(current - 1); schedule(); }
      if (event.key === 'ArrowRight') { event.preventDefault(); showSlide(current + 1); schedule(); }
    });
    document.addEventListener('visibilitychange', schedule);
    showSlide(0);
    updateToggle();
    schedule();
  });

  const showStatus = (message, kind) => {
    status.textContent = message;
    status.className = `form-status ${kind}`;
    status.hidden = false;
    status.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
  };

  const serviceSearch = document.querySelector('#service-search');
  const serviceDirectory = document.querySelector('#service-directory');
  const serviceCount = document.querySelector('#service-result-count');
  const serviceEmptyState = document.querySelector('#service-empty-state');
  if (serviceSearch && serviceDirectory) {
    const serviceCards = [...serviceDirectory.querySelectorAll('.directory-card')];
    const filterServices = () => {
      const query = serviceSearch.value.trim().toLocaleLowerCase();
      let visible = 0;
      serviceCards.forEach((card) => {
        const searchable = `${card.dataset.searchText || ''} ${card.textContent || ''}`.toLocaleLowerCase();
        const matches = !query || searchable.includes(query);
        card.hidden = !matches;
        if (matches) visible += 1;
      });
      if (serviceCount) serviceCount.textContent = `Showing ${visible} of ${serviceCards.length} services`;
      if (serviceEmptyState) serviceEmptyState.hidden = visible !== 0;
    };
    serviceSearch.addEventListener('input', filterServices);
  }

  form?.addEventListener('submit', async (event) => {
    event.preventDefault();
    status.hidden = true;
    if (!form.reportValidity()) return;

    const data = new FormData(form);
    const payload = {
      fullName: String(data.get('fullName') || '').trim(),
      businessEmail: String(data.get('businessEmail') || '').trim(),
      companyName: String(data.get('companyName') || '').trim(),
      roleDepartment: String(data.get('roleDepartment') || '').trim(),
      serviceOfInterest: String(data.get('serviceOfInterest') || ''),
      description: String(data.get('description') || '').trim(),
      preferredContact: String(data.get('preferredContact') || '').trim(),
      landingPage: window.location.pathname,
      website: String(data.get('website') || '').trim(),
      consent: data.get('consent') === 'on'
    };

    submitButton.disabled = true;
    submitButton.setAttribute('aria-busy', 'true');
    const originalLabel = submitButton.innerHTML;
    submitButton.innerHTML = 'Sending…';

    try {
      const response = await fetch('/api/consultations', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'Accept': 'application/json' },
        body: JSON.stringify(payload),
        signal: AbortSignal.timeout(15000)
      });
      if (!response.ok) throw new Error(`Request failed with status ${response.status}`);
      form.reset();
      showStatus('Thank you for reaching out. Your enquiry has been received by Infeara Technologies.', 'success');
    } catch (error) {
      showStatus('We couldn’t send your enquiry just now. Please try again in a little while.', 'error');
    } finally {
      submitButton.disabled = false;
      submitButton.removeAttribute('aria-busy');
      submitButton.innerHTML = originalLabel;
    }
  });
})();
