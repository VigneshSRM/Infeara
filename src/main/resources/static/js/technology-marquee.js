(() => {
  document.querySelectorAll('[data-technology-marquee]').forEach(marquee => {
    const button = marquee.querySelector('[data-technology-marquee-toggle]');
    if (!button) return;
    button.addEventListener('click', () => {
      const paused = marquee.toggleAttribute('data-paused');
      button.setAttribute('aria-pressed', String(paused));
      button.textContent = paused ? 'Play animation' : 'Pause animation';
      button.setAttribute('aria-label', paused ? 'Play technology logo animation' : 'Pause technology logo animation');
    });
  });
})();
