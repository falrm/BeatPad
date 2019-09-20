/**
 * @file synth.h<br>
 * @brief Embeddable SoundFont synthesizer<br>
 * * You create a new synthesizer with new_fluid_synth() and you destroy<br>
 * if with delete_fluid_synth(). Use the settings structure to specify<br>
 * the synthesizer characteristics.<br>
 * * You have to load a SoundFont in order to hear any sound. For that<br>
 * you use the fluid_synth_sfload() function.<br>
 * * You can use the audio driver functions described below to open<br>
 * the audio device and create a background audio thread.<br>
 * * The API for sending MIDI events is probably what you expect:<br>
 * fluid_synth_noteon(), fluid_synth_noteoff(), ...<br>
 * Original signature : <code>fluid_synth_t* new_fluid_synth(fluid_settings_t*)</code>
 */
fluid_synth_t* new_fluid_synth(fluid_settings_t* settings);
/** Original signature : <code>void delete_fluid_synth(fluid_synth_t*)</code> */
void delete_fluid_synth(fluid_synth_t* synth);
/** Original signature : <code>fluid_settings_t* fluid_synth_get_settings(fluid_synth_t*)</code> */
fluid_settings_t* fluid_synth_get_settings(fluid_synth_t* synth);
/**
 * MIDI channel messages<br>
 * Original signature : <code>int fluid_synth_noteon(fluid_synth_t*, int, int, int)</code>
 */
int fluid_synth_noteon(fluid_synth_t* synth, int chan, int key, int vel);
/** Original signature : <code>int fluid_synth_noteoff(fluid_synth_t*, int, int)</code> */
int fluid_synth_noteoff(fluid_synth_t* synth, int chan, int key);
/** Original signature : <code>int fluid_synth_cc(fluid_synth_t*, int, int, int)</code> */
int fluid_synth_cc(fluid_synth_t* synth, int chan, int ctrl, int val);
/** Original signature : <code>int fluid_synth_get_cc(fluid_synth_t*, int, int, int*)</code> */
int fluid_synth_get_cc(fluid_synth_t* synth, int chan, int ctrl, int* pval);
/** Original signature : <code>int fluid_synth_sysex(fluid_synth_t*, const char*, int, char*, int*, int*, int)</code> */
int fluid_synth_sysex(fluid_synth_t* synth, const char* data, int len, char* response, int* response_len, int* handled, int dryrun);
/** Original signature : <code>int fluid_synth_pitch_bend(fluid_synth_t*, int, int)</code> */
int fluid_synth_pitch_bend(fluid_synth_t* synth, int chan, int val);
/** Original signature : <code>int fluid_synth_get_pitch_bend(fluid_synth_t*, int, int*)</code> */
int fluid_synth_get_pitch_bend(fluid_synth_t* synth, int chan, int* ppitch_bend);
/** Original signature : <code>int fluid_synth_pitch_wheel_sens(fluid_synth_t*, int, int)</code> */
int fluid_synth_pitch_wheel_sens(fluid_synth_t* synth, int chan, int val);
/** Original signature : <code>int fluid_synth_get_pitch_wheel_sens(fluid_synth_t*, int, int*)</code> */
int fluid_synth_get_pitch_wheel_sens(fluid_synth_t* synth, int chan, int* pval);
/** Original signature : <code>int fluid_synth_program_change(fluid_synth_t*, int, int)</code> */
int fluid_synth_program_change(fluid_synth_t* synth, int chan, int program);
/** Original signature : <code>int fluid_synth_channel_pressure(fluid_synth_t*, int, int)</code> */
int fluid_synth_channel_pressure(fluid_synth_t* synth, int chan, int val);
/** Original signature : <code>int fluid_synth_key_pressure(fluid_synth_t*, int, int, int)</code> */
int fluid_synth_key_pressure(fluid_synth_t* synth, int chan, int key, int val);
/** Original signature : <code>int fluid_synth_bank_select(fluid_synth_t*, int, int)</code> */
int fluid_synth_bank_select(fluid_synth_t* synth, int chan, int bank);
/** Original signature : <code>int fluid_synth_sfont_select(fluid_synth_t*, int, int)</code> */
int fluid_synth_sfont_select(fluid_synth_t* synth, int chan, int sfont_id);
/** Original signature : <code>int fluid_synth_program_select(fluid_synth_t*, int, int, int, int)</code> */
int fluid_synth_program_select(fluid_synth_t* synth, int chan, int sfont_id, int bank_num, int preset_num);
/** Original signature : <code>int fluid_synth_program_select_by_sfont_name(fluid_synth_t*, int, const char*, int, int)</code> */
int fluid_synth_program_select_by_sfont_name(fluid_synth_t* synth, int chan, const char* sfont_name, int bank_num, int preset_num);
/** Original signature : <code>int fluid_synth_get_program(fluid_synth_t*, int, int*, int*, int*)</code> */
int fluid_synth_get_program(fluid_synth_t* synth, int chan, int* sfont_id, int* bank_num, int* preset_num);
/** Original signature : <code>int fluid_synth_unset_program(fluid_synth_t*, int)</code> */
int fluid_synth_unset_program(fluid_synth_t* synth, int chan);
/** Original signature : <code>int fluid_synth_program_reset(fluid_synth_t*)</code> */
int fluid_synth_program_reset(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_system_reset(fluid_synth_t*)</code> */
int fluid_synth_system_reset(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_all_notes_off(fluid_synth_t*, int)</code> */
int fluid_synth_all_notes_off(fluid_synth_t* synth, int chan);
/** Original signature : <code>int fluid_synth_all_sounds_off(fluid_synth_t*, int)</code> */
int fluid_synth_all_sounds_off(fluid_synth_t* synth, int chan);
/** The midi channel type used by fluid_synth_set_channel_type() */
enum fluid_midi_channel_type {
	CHANNEL_TYPE_MELODIC = 0 /**< Melodic midi channel */,
	CHANNEL_TYPE_DRUM = 1 /**< Drum midi channel */
};
/** Original signature : <code>int fluid_synth_set_channel_type(fluid_synth_t*, int, int)</code> */
int fluid_synth_set_channel_type(fluid_synth_t* synth, int chan, int type);
/**
 * Low level access<br>
 * Original signature : <code>fluid_preset_t* fluid_synth_get_channel_preset(fluid_synth_t*, int)</code>
 */
fluid_preset_t* fluid_synth_get_channel_preset(fluid_synth_t* synth, int chan);
/** Original signature : <code>int fluid_synth_start(fluid_synth_t*, unsigned int, fluid_preset_t*, int, int, int, int)</code> */
int fluid_synth_start(fluid_synth_t* synth, unsigned int id, fluid_preset_t* preset, int audio_chan, int midi_chan, int key, int vel);
/** Original signature : <code>int fluid_synth_stop(fluid_synth_t*, unsigned int)</code> */
int fluid_synth_stop(fluid_synth_t* synth, unsigned int id);
/**
 * SoundFont management<br>
 * Original signature : <code>int fluid_synth_sfload(fluid_synth_t*, const char*, int)</code>
 */
int fluid_synth_sfload(fluid_synth_t* synth, const char* filename, int reset_presets);
/** Original signature : <code>int fluid_synth_sfreload(fluid_synth_t*, int)</code> */
int fluid_synth_sfreload(fluid_synth_t* synth, int id);
/** Original signature : <code>int fluid_synth_sfunload(fluid_synth_t*, int, int)</code> */
int fluid_synth_sfunload(fluid_synth_t* synth, int id, int reset_presets);
/** Original signature : <code>int fluid_synth_add_sfont(fluid_synth_t*, fluid_sfont_t*)</code> */
int fluid_synth_add_sfont(fluid_synth_t* synth, fluid_sfont_t* sfont);
/** Original signature : <code>int fluid_synth_remove_sfont(fluid_synth_t*, fluid_sfont_t*)</code> */
int fluid_synth_remove_sfont(fluid_synth_t* synth, fluid_sfont_t* sfont);
/** Original signature : <code>int fluid_synth_sfcount(fluid_synth_t*)</code> */
int fluid_synth_sfcount(fluid_synth_t* synth);
/** Original signature : <code>fluid_sfont_t* fluid_synth_get_sfont(fluid_synth_t*, unsigned int)</code> */
fluid_sfont_t* fluid_synth_get_sfont(fluid_synth_t* synth, unsigned int num);
/** Original signature : <code>fluid_sfont_t* fluid_synth_get_sfont_by_id(fluid_synth_t*, int)</code> */
fluid_sfont_t* fluid_synth_get_sfont_by_id(fluid_synth_t* synth, int id);
/** Original signature : <code>fluid_sfont_t* fluid_synth_get_sfont_by_name(fluid_synth_t*, const char*)</code> */
fluid_sfont_t* fluid_synth_get_sfont_by_name(fluid_synth_t* synth, const char* name);
/** Original signature : <code>int fluid_synth_set_bank_offset(fluid_synth_t*, int, int)</code> */
int fluid_synth_set_bank_offset(fluid_synth_t* synth, int sfont_id, int offset);
/** Original signature : <code>int fluid_synth_get_bank_offset(fluid_synth_t*, int)</code> */
int fluid_synth_get_bank_offset(fluid_synth_t* synth, int sfont_id);
/**
 * Reverb<br>
 * Original signature : <code>int fluid_synth_set_reverb(fluid_synth_t*, double, double, double, double)</code>
 */
int fluid_synth_set_reverb(fluid_synth_t* synth, double roomsize, double damping, double width, double level);
/** Original signature : <code>int fluid_synth_set_reverb_roomsize(fluid_synth_t*, double)</code> */
int fluid_synth_set_reverb_roomsize(fluid_synth_t* synth, double roomsize);
/** Original signature : <code>int fluid_synth_set_reverb_damp(fluid_synth_t*, double)</code> */
int fluid_synth_set_reverb_damp(fluid_synth_t* synth, double damping);
/** Original signature : <code>int fluid_synth_set_reverb_width(fluid_synth_t*, double)</code> */
int fluid_synth_set_reverb_width(fluid_synth_t* synth, double width);
/** Original signature : <code>int fluid_synth_set_reverb_level(fluid_synth_t*, double)</code> */
int fluid_synth_set_reverb_level(fluid_synth_t* synth, double level);
/** Original signature : <code>void fluid_synth_set_reverb_on(fluid_synth_t*, int)</code> */
void fluid_synth_set_reverb_on(fluid_synth_t* synth, int on);
/** Original signature : <code>double fluid_synth_get_reverb_roomsize(fluid_synth_t*)</code> */
double fluid_synth_get_reverb_roomsize(fluid_synth_t* synth);
/** Original signature : <code>double fluid_synth_get_reverb_damp(fluid_synth_t*)</code> */
double fluid_synth_get_reverb_damp(fluid_synth_t* synth);
/** Original signature : <code>double fluid_synth_get_reverb_level(fluid_synth_t*)</code> */
double fluid_synth_get_reverb_level(fluid_synth_t* synth);
/** Original signature : <code>double fluid_synth_get_reverb_width(fluid_synth_t*)</code> */
double fluid_synth_get_reverb_width(fluid_synth_t* synth);
/** Chorus modulation waveform type. */
enum fluid_chorus_mod {
	FLUID_CHORUS_MOD_SINE = 0 /**< Sine wave chorus modulation */,
	FLUID_CHORUS_MOD_TRIANGLE = 1 /**< Triangle wave chorus modulation */
};
/** Original signature : <code>int fluid_synth_set_chorus(fluid_synth_t*, int, double, double, double, int)</code> */
int fluid_synth_set_chorus(fluid_synth_t* synth, int nr, double level, double speed, double depth_ms, int type);
/** Original signature : <code>int fluid_synth_set_chorus_nr(fluid_synth_t*, int)</code> */
int fluid_synth_set_chorus_nr(fluid_synth_t* synth, int nr);
/** Original signature : <code>int fluid_synth_set_chorus_level(fluid_synth_t*, double)</code> */
int fluid_synth_set_chorus_level(fluid_synth_t* synth, double level);
/** Original signature : <code>int fluid_synth_set_chorus_speed(fluid_synth_t*, double)</code> */
int fluid_synth_set_chorus_speed(fluid_synth_t* synth, double speed);
/** Original signature : <code>int fluid_synth_set_chorus_depth(fluid_synth_t*, double)</code> */
int fluid_synth_set_chorus_depth(fluid_synth_t* synth, double depth_ms);
/** Original signature : <code>int fluid_synth_set_chorus_type(fluid_synth_t*, int)</code> */
int fluid_synth_set_chorus_type(fluid_synth_t* synth, int type);
/** Original signature : <code>void fluid_synth_set_chorus_on(fluid_synth_t*, int)</code> */
void fluid_synth_set_chorus_on(fluid_synth_t* synth, int on);
/** Original signature : <code>int fluid_synth_get_chorus_nr(fluid_synth_t*)</code> */
int fluid_synth_get_chorus_nr(fluid_synth_t* synth);
/** Original signature : <code>double fluid_synth_get_chorus_level(fluid_synth_t*)</code> */
double fluid_synth_get_chorus_level(fluid_synth_t* synth);
/** Original signature : <code>double fluid_synth_get_chorus_speed(fluid_synth_t*)</code> */
double fluid_synth_get_chorus_speed(fluid_synth_t* synth);
/** Original signature : <code>double fluid_synth_get_chorus_depth(fluid_synth_t*)</code> */
double fluid_synth_get_chorus_depth(fluid_synth_t* synth);
/**
 * see fluid_chorus_mod<br>
 * Original signature : <code>int fluid_synth_get_chorus_type(fluid_synth_t*)</code>
 */
int fluid_synth_get_chorus_type(fluid_synth_t* synth);
/**
 * Audio and MIDI channels<br>
 * Original signature : <code>int fluid_synth_count_midi_channels(fluid_synth_t*)</code>
 */
int fluid_synth_count_midi_channels(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_count_audio_channels(fluid_synth_t*)</code> */
int fluid_synth_count_audio_channels(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_count_audio_groups(fluid_synth_t*)</code> */
int fluid_synth_count_audio_groups(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_count_effects_channels(fluid_synth_t*)</code> */
int fluid_synth_count_effects_channels(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_count_effects_groups(fluid_synth_t*)</code> */
int fluid_synth_count_effects_groups(fluid_synth_t* synth);
/**
 * Synthesis parameters<br>
 * Original signature : <code>void fluid_synth_set_sample_rate(fluid_synth_t*, float)</code>
 */
void fluid_synth_set_sample_rate(fluid_synth_t* synth, float sample_rate);
/** Original signature : <code>void fluid_synth_set_gain(fluid_synth_t*, float)</code> */
void fluid_synth_set_gain(fluid_synth_t* synth, float gain);
/** Original signature : <code>float fluid_synth_get_gain(fluid_synth_t*)</code> */
float fluid_synth_get_gain(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_set_polyphony(fluid_synth_t*, int)</code> */
int fluid_synth_set_polyphony(fluid_synth_t* synth, int polyphony);
/** Original signature : <code>int fluid_synth_get_polyphony(fluid_synth_t*)</code> */
int fluid_synth_get_polyphony(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_get_active_voice_count(fluid_synth_t*)</code> */
int fluid_synth_get_active_voice_count(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_get_internal_bufsize(fluid_synth_t*)</code> */
int fluid_synth_get_internal_bufsize(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_set_interp_method(fluid_synth_t*, int, int)</code> */
int fluid_synth_set_interp_method(fluid_synth_t* synth, int chan, int interp_method);
/** Synthesis interpolation method. */
enum fluid_interp {
	FLUID_INTERP_NONE = 0 /**< No interpolation: Fastest, but questionable audio quality */,
	FLUID_INTERP_LINEAR = 1 /**< Straight-line interpolation: A bit slower, reasonable audio quality */,
	FLUID_INTERP_4THORDER = 4 /**< Fourth-order interpolation, good quality, the default */,
	FLUID_INTERP_7THORDER = 7 /**< Seventh-order interpolation */,
	FLUID_INTERP_DEFAULT = FLUID_INTERP_4THORDER /**< Default interpolation method */,
	FLUID_INTERP_HIGHEST = FLUID_INTERP_7THORDER /**< Highest interpolation method */
};
/**
 * Generator interface<br>
 * Original signature : <code>int fluid_synth_set_gen(fluid_synth_t*, int, int, float)</code>
 */
int fluid_synth_set_gen(fluid_synth_t* synth, int chan, int param, float value);
/** Original signature : <code>float fluid_synth_get_gen(fluid_synth_t*, int, int)</code> */
float fluid_synth_get_gen(fluid_synth_t* synth, int chan, int param);
/**
 * Tuning<br>
 * Original signature : <code>int fluid_synth_activate_key_tuning(fluid_synth_t*, int, int, const char*, const double*, int)</code>
 */
int fluid_synth_activate_key_tuning(fluid_synth_t* synth, int bank, int prog, const char* name, const double* pitch, int apply);
/** Original signature : <code>int fluid_synth_activate_octave_tuning(fluid_synth_t*, int, int, const char*, const double*, int)</code> */
int fluid_synth_activate_octave_tuning(fluid_synth_t* synth, int bank, int prog, const char* name, const double* pitch, int apply);
/** Original signature : <code>int fluid_synth_tune_notes(fluid_synth_t*, int, int, int, const int*, const double*, int)</code> */
int fluid_synth_tune_notes(fluid_synth_t* synth, int bank, int prog, int len, const int* keys, const double* pitch, int apply);
/** Original signature : <code>int fluid_synth_activate_tuning(fluid_synth_t*, int, int, int, int)</code> */
int fluid_synth_activate_tuning(fluid_synth_t* synth, int chan, int bank, int prog, int apply);
/** Original signature : <code>int fluid_synth_deactivate_tuning(fluid_synth_t*, int, int)</code> */
int fluid_synth_deactivate_tuning(fluid_synth_t* synth, int chan, int apply);
/** Original signature : <code>void fluid_synth_tuning_iteration_start(fluid_synth_t*)</code> */
void fluid_synth_tuning_iteration_start(fluid_synth_t* synth);
/** Original signature : <code>int fluid_synth_tuning_iteration_next(fluid_synth_t*, int*, int*)</code> */
int fluid_synth_tuning_iteration_next(fluid_synth_t* synth, int* bank, int* prog);
/** Original signature : <code>int fluid_synth_tuning_dump(fluid_synth_t*, int, int, char*, int, double*)</code> */
int fluid_synth_tuning_dump(fluid_synth_t* synth, int bank, int prog, char* name, int len, double* pitch);
/**
 * Misc<br>
 * Original signature : <code>double fluid_synth_get_cpu_load(fluid_synth_t*)</code>
 */
double fluid_synth_get_cpu_load(fluid_synth_t* synth);
/** Original signature : <code>char* fluid_synth_error(fluid_synth_t*)</code> */
const char* fluid_synth_error(fluid_synth_t* synth);
/** Enum used with fluid_synth_add_default_mod() to specify how to handle duplicate modulators. */
enum fluid_synth_add_mod {
	FLUID_SYNTH_OVERWRITE /**< Overwrite any existing matching modulator */,
	FLUID_SYNTH_ADD /**< Add (sum) modulator amounts */
};
/** Original signature : <code>int fluid_synth_add_default_mod(fluid_synth_t*, const fluid_mod_t*, int)</code> */
int fluid_synth_add_default_mod(fluid_synth_t* synth, const fluid_mod_t* mod, int mode);
/** Original signature : <code>int fluid_synth_remove_default_mod(fluid_synth_t*, const fluid_mod_t*)</code> */
int fluid_synth_remove_default_mod(fluid_synth_t* synth, const fluid_mod_t* mod);
/**
 * Synthesizer plugin<br>
 * * To create a synthesizer plugin, create the synthesizer as<br>
 * explained above. Once the synthesizer is created you can call<br>
 * any of the functions below to get the audio.<br>
 * Original signature : <code>int fluid_synth_write_s16(fluid_synth_t*, int, void*, int, int, void*, int, int)</code>
 */
int fluid_synth_write_s16(fluid_synth_t* synth, int len, void* lout, int loff, int lincr, void* rout, int roff, int rincr);
/** Original signature : <code>int fluid_synth_write_float(fluid_synth_t*, int, void*, int, int, void*, int, int)</code> */
int fluid_synth_write_float(fluid_synth_t* synth, int len, void* lout, int loff, int lincr, void* rout, int roff, int rincr);
/** Original signature : <code>int fluid_synth_nwrite_float(fluid_synth_t*, int, float**, float**, float**, float**)</code> */
int fluid_synth_nwrite_float(fluid_synth_t* synth, int len, float** left, float** right, float** fx_left, float** fx_right);
/** Original signature : <code>int fluid_synth_process(fluid_synth_t*, int, int, float*[], int, float*[])</code> */
int fluid_synth_process(fluid_synth_t* synth, int len, int nfx, float* fx[], int nout, float* out[]);
/**
 * Synthesizer's interface to handle SoundFont loaders<br>
 * Original signature : <code>void fluid_synth_add_sfloader(fluid_synth_t*, fluid_sfloader_t*)</code>
 */
void fluid_synth_add_sfloader(fluid_synth_t* synth, fluid_sfloader_t* loader);
/** Original signature : <code>fluid_voice_t* fluid_synth_alloc_voice(fluid_synth_t*, fluid_sample_t*, int, int, int)</code> */
fluid_voice_t* fluid_synth_alloc_voice(fluid_synth_t* synth, fluid_sample_t* sample, int channum, int key, int vel);
/** Original signature : <code>void fluid_synth_start_voice(fluid_synth_t*, fluid_voice_t*)</code> */
void fluid_synth_start_voice(fluid_synth_t* synth, fluid_voice_t* voice);
/** Original signature : <code>void fluid_synth_get_voicelist(fluid_synth_t*, fluid_voice_t*[], int, int)</code> */
void fluid_synth_get_voicelist(fluid_synth_t* synth, fluid_voice_t* buf[], int bufsize, int ID);
/** Original signature : <code>int fluid_synth_handle_midi_event(void*, fluid_midi_event_t*)</code> */
int fluid_synth_handle_midi_event(void* data, fluid_midi_event_t* event);
/** Specifies the type of filter to use for the custom IIR filter */
enum fluid_iir_filter_type {
	FLUID_IIR_DISABLED = 0 /**< Custom IIR filter is not operating */,
	FLUID_IIR_LOWPASS /**< Custom IIR filter is operating as low-pass filter */,
	FLUID_IIR_HIGHPASS /**< Custom IIR filter is operating as high-pass filter */,
	FLUID_IIR_LAST /**< @internal Value defines the count of filter types (#fluid_iir_filter_type) @warning This symbol is not part of the public API and ABI stability guarantee and may change at any time! */
};
/** Specifies optional settings to use for the custom IIR filter */
enum fluid_iir_filter_flags {
	FLUID_IIR_Q_LINEAR = 1 << 0 /**< The Soundfont spec requires the filter Q to be interpreted in dB. If this flag is set the filter Q is instead assumed to be in a linear range */,
	FLUID_IIR_Q_ZERO_OFF = 1 << 1 /**< If this flag the filter is switched off if Q == 0 (prior to any transformation) */,
	FLUID_IIR_NO_GAIN_AMP = 1 << 2 /**< The Soundfont spec requires to correct the gain of the filter depending on the filter's Q. If this flag is set the filter gain will not be corrected. */
};
/** Original signature : <code>int fluid_synth_set_custom_filter(fluid_synth_t*, int, int)</code> */
int fluid_synth_set_custom_filter(fluid_synth_t* fluid_synth_tPtr1, int type, int flags);
/**
 * LADSPA<br>
 * Original signature : <code>fluid_ladspa_fx_t* fluid_synth_get_ladspa_fx(fluid_synth_t*)</code>
 */
fluid_ladspa_fx_t* fluid_synth_get_ladspa_fx(fluid_synth_t* synth);
/**
 * Interface to poly/mono mode variables<br>
 * Channel mode bits OR-ed together so that it matches with the midi spec: poly omnion (0), mono omnion (1), poly omnioff (2), mono omnioff (3)
 */
enum fluid_channel_mode_flags {
	FLUID_CHANNEL_POLY_OFF = 0x01 /**< if flag is set, the basic channel is in mono on state, if not set poly is on */,
	FLUID_CHANNEL_OMNI_OFF = 0x02 /**< if flag is set, the basic channel is in omni off state, if not set omni is on */
};
/** Indicates the breath mode a channel is set to */
enum fluid_channel_breath_flags {
	FLUID_CHANNEL_BREATH_POLY = 0x10 /**< when channel is poly, this flag indicates that the default velocity to initial attenuation modulator is replaced by a breath to initial attenuation modulator */,
	FLUID_CHANNEL_BREATH_MONO = 0x20 /**< when channel is mono, this flag indicates that the default velocity to initial attenuation modulator is replaced by a breath modulator */,
	FLUID_CHANNEL_BREATH_SYNC = 0x40 /**< when channel is mono, this flag indicates that the breath controler(MSB)triggers noteon/noteoff on the running note */
};
/** Indicates the mode a basic channel is set to */
enum fluid_basic_channel_modes {
	FLUID_CHANNEL_MODE_MASK = (FLUID_CHANNEL_OMNI_OFF | FLUID_CHANNEL_POLY_OFF) /**< Mask Poly and Omni bits of #fluid_channel_mode_flags, usually only used internally */,
	FLUID_CHANNEL_MODE_OMNION_POLY = FLUID_CHANNEL_MODE_MASK & (~FLUID_CHANNEL_OMNI_OFF & ~FLUID_CHANNEL_POLY_OFF) /**< corresponds to MIDI mode 0 */,
	FLUID_CHANNEL_MODE_OMNION_MONO = FLUID_CHANNEL_MODE_MASK & (~FLUID_CHANNEL_OMNI_OFF & FLUID_CHANNEL_POLY_OFF) /**< corresponds to MIDI mode 1 */,
	FLUID_CHANNEL_MODE_OMNIOFF_POLY = FLUID_CHANNEL_MODE_MASK & (FLUID_CHANNEL_OMNI_OFF & ~FLUID_CHANNEL_POLY_OFF) /**< corresponds to MIDI mode 2 */,
	FLUID_CHANNEL_MODE_OMNIOFF_MONO = FLUID_CHANNEL_MODE_MASK & (FLUID_CHANNEL_OMNI_OFF | FLUID_CHANNEL_POLY_OFF) /**< corresponds to MIDI mode 3 */,
	FLUID_CHANNEL_MODE_LAST /**< @internal Value defines the count of basic channel modes (#fluid_basic_channel_modes) @warning This symbol is not part of the public API and ABI stability guarantee and may change at any time! */
};
/** Original signature : <code>int fluid_synth_reset_basic_channel(fluid_synth_t*, int)</code> */
int fluid_synth_reset_basic_channel(fluid_synth_t* synth, int chan);
/** Original signature : <code>int fluid_synth_get_basic_channel(fluid_synth_t*, int, int*, int*, int*)</code> */
int fluid_synth_get_basic_channel(fluid_synth_t* synth, int chan, int* basic_chan_out, int* mode_chan_out, int* basic_val_out);
/** Original signature : <code>int fluid_synth_set_basic_channel(fluid_synth_t*, int, int, int)</code> */
int fluid_synth_set_basic_channel(fluid_synth_t* synth, int chan, int mode, int val);
/**
 * Interface to mono legato mode<br>
 * Indicates the legato mode a channel is set to<br>
 * n1,n2,n3,.. is a legato passage. n1 is the first note, and n2,n3,n4 are played legato with previous note.
 */
enum fluid_channel_legato_mode {
	FLUID_CHANNEL_LEGATO_MODE_RETRIGGER /**< Mode 0 - Release previous note, start a new note */,
	FLUID_CHANNEL_LEGATO_MODE_MULTI_RETRIGGER /**< Mode 1 - On contiguous notes retrigger in attack section using current value, shape attack using current dynamic and make use of previous voices if any */,
	FLUID_CHANNEL_LEGATO_MODE_LAST /**< @internal Value defines the count of legato modes (#fluid_channel_legato_mode) @warning This symbol is not part of the public API and ABI stability guarantee and may change at any time! */
};
/** Original signature : <code>int fluid_synth_set_legato_mode(fluid_synth_t*, int, int)</code> */
int fluid_synth_set_legato_mode(fluid_synth_t* synth, int chan, int legatomode);
/** Original signature : <code>int fluid_synth_get_legato_mode(fluid_synth_t*, int, int*)</code> */
int fluid_synth_get_legato_mode(fluid_synth_t* synth, int chan, int* legatomode);
/**
 * Interface to portamento mode<br>
 * Indicates the portamento mode a channel is set to
 */
enum fluid_channel_portamento_mode {
	FLUID_CHANNEL_PORTAMENTO_MODE_EACH_NOTE /**< Mode 0 - Portamento on each note (staccato or legato) */,
	FLUID_CHANNEL_PORTAMENTO_MODE_LEGATO_ONLY /**< Mode 1 - Portamento only on legato note */,
	FLUID_CHANNEL_PORTAMENTO_MODE_STACCATO_ONLY /**< Mode 2 - Portamento only on staccato note */,
	FLUID_CHANNEL_PORTAMENTO_MODE_LAST /**< @internal Value defines the count of portamento modes (#fluid_channel_portamento_mode) @warning This symbol is not part of the public API and ABI stability guarantee and may change at any time! */
};
/** Original signature : <code>int fluid_synth_set_portamento_mode(fluid_synth_t*, int, int)</code> */
int fluid_synth_set_portamento_mode(fluid_synth_t* synth, int chan, int portamentomode);
/** Original signature : <code>int fluid_synth_get_portamento_mode(fluid_synth_t*, int, int*)</code> */
int fluid_synth_get_portamento_mode(fluid_synth_t* synth, int chan, int* portamentomode);
/**
 * Interface to breath mode<br>
 * Original signature : <code>int fluid_synth_set_breath_mode(fluid_synth_t*, int, int)</code>
 */
int fluid_synth_set_breath_mode(fluid_synth_t* synth, int chan, int breathmode);
/** Original signature : <code>int fluid_synth_get_breath_mode(fluid_synth_t*, int, int*)</code> */
int fluid_synth_get_breath_mode(fluid_synth_t* synth, int chan, int* breathmode);
/** Some notification enums for presets and samples. */
enum {
	FLUID_PRESET_SELECTED /**< Preset selected notify */,
	FLUID_PRESET_UNSELECTED /**< Preset unselected notify */,
	FLUID_SAMPLE_DONE /**< Sample no longer needed notify */
};
/** Indicates the type of a sample used by the _fluid_sample_t::sampletype field. */
enum fluid_sample_type {
	FLUID_SAMPLETYPE_MONO = 0x1 /**< Used for mono samples */,
	FLUID_SAMPLETYPE_RIGHT = 0x2 /**< Used for right samples of a stereo pair */,
	FLUID_SAMPLETYPE_LEFT = 0x4 /**< Used for left samples of a stereo pair */,
	FLUID_SAMPLETYPE_LINKED = 0x8 /**< Currently not used */,
	FLUID_SAMPLETYPE_OGG_VORBIS = 0x10 /**< Used for Ogg Vorbis compressed samples @since 1.1.7 */,
	FLUID_SAMPLETYPE_ROM = 0x8000 /**< Indicates ROM samples, causes sample to be ignored */
};
/**
 * Method to load an instrument file (does not actually need to be a real file name,<br>
 * could be another type of string identifier that the \a loader understands).<br>
 * @param loader SoundFont loader<br>
 * @param filename File name or other string identifier<br>
 * @return The loaded instrument file (SoundFont) or NULL if an error occured.
 */
typedef fluid_sfont_t* (*fluid_sfloader_load_t)(fluid_sfloader_t* loader, const char* filename) fluid_sfloader_load_t;
/**
 * The free method should free the memory allocated for a fluid_sfloader_t instance in<br>
 * addition to any private data. Any custom user provided cleanup function must ultimately call<br>
 * delete_fluid_sfloader() to ensure proper cleanup of the #fluid_sfloader_t struct. If no private data<br>
 * needs to be freed, setting this to delete_fluid_sfloader() is sufficient.<br>
 * @param loader SoundFont loader
 */
typedef void (*fluid_sfloader_free_t)(fluid_sfloader_t* loader) fluid_sfloader_free_t;
/** Original signature : <code>fluid_sfloader_t* new_fluid_sfloader(fluid_sfloader_load_t, fluid_sfloader_free_t)</code> */
fluid_sfloader_t* new_fluid_sfloader(fluid_sfloader_load_t load, fluid_sfloader_free_t free);
/** Original signature : <code>void delete_fluid_sfloader(fluid_sfloader_t*)</code> */
void delete_fluid_sfloader(fluid_sfloader_t* loader);
/** Original signature : <code>fluid_sfloader_t* new_fluid_defsfloader(fluid_settings_t*)</code> */
fluid_sfloader_t* new_fluid_defsfloader(fluid_settings_t* settings);
/**
 * Opens the file or memory indicated by \c filename in binary read mode.<br>
 * \c filename matches the string provided during the fluid_synth_sfload() call.<br>
 * * @return returns a file handle on success, NULL otherwise
 */
typedef void* (*fluid_sfloader_callback_open_t)(const char* filename) fluid_sfloader_callback_open_t;
/**
 * Reads \c count bytes to the specified buffer \c buf.<br>
 * * @return returns #FLUID_OK if exactly \c count bytes were successfully read, else returns #FLUID_FAILED and leaves \a buf unmodified.
 */
typedef int (*fluid_sfloader_callback_read_t)(void* buf, int count, void* handle) fluid_sfloader_callback_read_t;
/**
 * Same purpose and behaviour as fseek.<br>
 * * @param origin either \c SEEK_SET, \c SEEK_CUR or \c SEEK_END<br>
 * * @return returns #FLUID_OK if the seek was successfully performed while not seeking beyond a buffer or file, #FLUID_FAILED otherwise
 */
typedef int (*fluid_sfloader_callback_seek_t)(void* handle, long offset, int origin) fluid_sfloader_callback_seek_t;
/**
 * Closes the handle returned by #fluid_sfloader_callback_open_t and frees used ressources.<br>
 * * @return returns #FLUID_OK on success, #FLUID_FAILED on error
 */
typedef int (*fluid_sfloader_callback_close_t)(void* handle) fluid_sfloader_callback_close_t;
/** @return returns current file offset or #FLUID_FAILED on error */
typedef long (*fluid_sfloader_callback_tell_t)(void* handle) fluid_sfloader_callback_tell_t;
/** Original signature : <code>int fluid_sfloader_set_callbacks(fluid_sfloader_t*, fluid_sfloader_callback_open_t, fluid_sfloader_callback_read_t, fluid_sfloader_callback_seek_t, fluid_sfloader_callback_tell_t, fluid_sfloader_callback_close_t)</code> */
int fluid_sfloader_set_callbacks(fluid_sfloader_t* loader, fluid_sfloader_callback_open_t open, fluid_sfloader_callback_read_t read, fluid_sfloader_callback_seek_t seek, fluid_sfloader_callback_tell_t tell, fluid_sfloader_callback_close_t close);
/** Original signature : <code>int fluid_sfloader_set_data(fluid_sfloader_t*, void*)</code> */
int fluid_sfloader_set_data(fluid_sfloader_t* loader, void* data);
/** Original signature : <code>void* fluid_sfloader_get_data(fluid_sfloader_t*)</code> */
void* fluid_sfloader_get_data(fluid_sfloader_t* loader);
/**
 * Method to return the name of a virtual SoundFont.<br>
 * @param sfont Virtual SoundFont<br>
 * @return The name of the virtual SoundFont.
 */
typedef const char* (*fluid_sfont_get_name_t)(fluid_sfont_t* sfont) fluid_sfont_get_name_t;
/**
 * Get a virtual SoundFont preset by bank and program numbers.<br>
 * @param sfont Virtual SoundFont<br>
 * @param bank MIDI bank number (0-16383)<br>
 * @param prenum MIDI preset number (0-127)<br>
 * @return Should return an allocated virtual preset or NULL if it could not<br>
 *   be found.
 */
typedef fluid_preset_t* (*fluid_sfont_get_preset_t)(fluid_sfont_t* sfont, int bank, int prenum) fluid_sfont_get_preset_t;
/**
 * Start virtual SoundFont preset iteration method.<br>
 * @param sfont Virtual SoundFont<br>
 * * Starts/re-starts virtual preset iteration in a SoundFont.
 */
typedef void (*fluid_sfont_iteration_start_t)(fluid_sfont_t* sfont) fluid_sfont_iteration_start_t;
/**
 * Virtual SoundFont preset iteration function.<br>
 * @param sfont Virtual SoundFont<br>
 * @return NULL when no more presets are available, otherwise the a pointer to the current preset<br>
 * * Returns preset information to the caller. The returned buffer is only valid until a subsequent<br>
 * call to this function.
 */
typedef fluid_preset_t* (*fluid_sfont_iteration_next_t)(fluid_sfont_t* sfont) fluid_sfont_iteration_next_t;
/**
 * Method to free a virtual SoundFont bank. Any custom user provided cleanup function must ultimately call<br>
 * delete_fluid_sfont() to ensure proper cleanup of the #fluid_sfont_t struct. If no private data<br>
 * needs to be freed, setting this to delete_fluid_sfont() is sufficient.<br>
 * @param sfont Virtual SoundFont to free.<br>
 * @return Should return 0 when it was able to free all resources or non-zero<br>
 *   if some of the samples could not be freed because they are still in use,<br>
 *   in which case the free will be tried again later, until success.
 */
typedef int (*fluid_sfont_free_t)(fluid_sfont_t* sfont) fluid_sfont_free_t;
/** Original signature : <code>fluid_sfont_t* new_fluid_sfont(fluid_sfont_get_name_t, fluid_sfont_get_preset_t, fluid_sfont_iteration_start_t, fluid_sfont_iteration_next_t, fluid_sfont_free_t)</code> */
fluid_sfont_t* new_fluid_sfont(fluid_sfont_get_name_t get_name, fluid_sfont_get_preset_t get_preset, fluid_sfont_iteration_start_t iter_start, fluid_sfont_iteration_next_t iter_next, fluid_sfont_free_t free);
/** Original signature : <code>int delete_fluid_sfont(fluid_sfont_t*)</code> */
int delete_fluid_sfont(fluid_sfont_t* sfont);
/** Original signature : <code>int fluid_sfont_set_data(fluid_sfont_t*, void*)</code> */
int fluid_sfont_set_data(fluid_sfont_t* sfont, void* data);
/** Original signature : <code>void* fluid_sfont_get_data(fluid_sfont_t*)</code> */
void* fluid_sfont_get_data(fluid_sfont_t* sfont);
/** Original signature : <code>int fluid_sfont_get_id(fluid_sfont_t*)</code> */
int fluid_sfont_get_id(fluid_sfont_t* sfont);
/** Original signature : <code>char* fluid_sfont_get_name(fluid_sfont_t*)</code> */
const char* fluid_sfont_get_name(fluid_sfont_t* sfont);
/** Original signature : <code>fluid_preset_t* fluid_sfont_get_preset(fluid_sfont_t*, int, int)</code> */
fluid_preset_t* fluid_sfont_get_preset(fluid_sfont_t* sfont, int bank, int prenum);
/** Original signature : <code>void fluid_sfont_iteration_start(fluid_sfont_t*)</code> */
void fluid_sfont_iteration_start(fluid_sfont_t* sfont);
/** Original signature : <code>fluid_preset_t* fluid_sfont_iteration_next(fluid_sfont_t*)</code> */
fluid_preset_t* fluid_sfont_iteration_next(fluid_sfont_t* sfont);
/**
 * Method to get a virtual SoundFont preset name.<br>
 * @param preset Virtual SoundFont preset<br>
 * @return Should return the name of the preset.  The returned string must be<br>
 *   valid for the duration of the virtual preset (or the duration of the<br>
 *   SoundFont, in the case of preset iteration).
 */
typedef const char* (*fluid_preset_get_name_t)(fluid_preset_t* preset) fluid_preset_get_name_t;
/**
 * Method to get a virtual SoundFont preset MIDI bank number.<br>
 * @param preset Virtual SoundFont preset<br>
 * @param return The bank number of the preset
 */
typedef int (*fluid_preset_get_banknum_t)(fluid_preset_t* preset) fluid_preset_get_banknum_t;
/**
 * Method to get a virtual SoundFont preset MIDI program number.<br>
 * @param preset Virtual SoundFont preset<br>
 * @param return The program number of the preset
 */
typedef int (*fluid_preset_get_num_t)(fluid_preset_t* preset) fluid_preset_get_num_t;
/**
 * Method to handle a noteon event (synthesize the instrument).<br>
 * @param preset Virtual SoundFont preset<br>
 * @param synth Synthesizer instance<br>
 * @param chan MIDI channel number of the note on event<br>
 * @param key MIDI note number (0-127)<br>
 * @param vel MIDI velocity (0-127)<br>
 * @return #FLUID_OK on success (0) or #FLUID_FAILED (-1) otherwise<br>
 * * This method may be called from within synthesis context and therefore<br>
 * should be as efficient as possible and not perform any operations considered<br>
 * bad for realtime audio output (memory allocations and other OS calls).<br>
 * * Call fluid_synth_alloc_voice() for every sample that has<br>
 * to be played. fluid_synth_alloc_voice() expects a pointer to a<br>
 * #fluid_sample_t structure and returns a pointer to the opaque<br>
 * #fluid_voice_t structure. To set or increment the values of a<br>
 * generator, use fluid_voice_gen_set() or fluid_voice_gen_incr(). When you are<br>
 * finished initializing the voice call fluid_voice_start() to<br>
 * start playing the synthesis voice.  Starting with FluidSynth 1.1.0 all voices<br>
 * created will be started at the same time.
 */
typedef int (*fluid_preset_noteon_t)(fluid_preset_t* preset, fluid_synth_t* synth, int chan, int key, int vel) fluid_preset_noteon_t;
/**
 * Method to free a virtual SoundFont preset. Any custom user provided cleanup function must ultimately call<br>
 * delete_fluid_preset() to ensure proper cleanup of the #fluid_preset_t struct. If no private data<br>
 * needs to be freed, setting this to delete_fluid_preset() is sufficient.<br>
 * @param preset Virtual SoundFont preset<br>
 * @return Should return 0
 */
typedef void (*fluid_preset_free_t)(fluid_preset_t* preset) fluid_preset_free_t;
/** Original signature : <code>fluid_preset_t* new_fluid_preset(fluid_sfont_t*, fluid_preset_get_name_t, fluid_preset_get_banknum_t, fluid_preset_get_num_t, fluid_preset_noteon_t, fluid_preset_free_t)</code> */
fluid_preset_t* new_fluid_preset(fluid_sfont_t* parent_sfont, fluid_preset_get_name_t get_name, fluid_preset_get_banknum_t get_bank, fluid_preset_get_num_t get_num, fluid_preset_noteon_t noteon, fluid_preset_free_t free);
/** Original signature : <code>void delete_fluid_preset(fluid_preset_t*)</code> */
void delete_fluid_preset(fluid_preset_t* preset);
/** Original signature : <code>int fluid_preset_set_data(fluid_preset_t*, void*)</code> */
int fluid_preset_set_data(fluid_preset_t* preset, void* data);
/** Original signature : <code>void* fluid_preset_get_data(fluid_preset_t*)</code> */
void* fluid_preset_get_data(fluid_preset_t* preset);
/** Original signature : <code>char* fluid_preset_get_name(fluid_preset_t*)</code> */
const char* fluid_preset_get_name(fluid_preset_t* preset);
/** Original signature : <code>int fluid_preset_get_banknum(fluid_preset_t*)</code> */
int fluid_preset_get_banknum(fluid_preset_t* preset);
/** Original signature : <code>int fluid_preset_get_num(fluid_preset_t*)</code> */
int fluid_preset_get_num(fluid_preset_t* preset);
/** Original signature : <code>fluid_sfont_t* fluid_preset_get_sfont(fluid_preset_t*)</code> */
fluid_sfont_t* fluid_preset_get_sfont(fluid_preset_t* preset);
/** Original signature : <code>fluid_sample_t* new_fluid_sample()</code> */
fluid_sample_t* new_fluid_sample();
/** Original signature : <code>void delete_fluid_sample(fluid_sample_t*)</code> */
void delete_fluid_sample(fluid_sample_t* sample);
/** Original signature : <code>size_t fluid_sample_sizeof()</code> */
size_t fluid_sample_sizeof();
/** Original signature : <code>int fluid_sample_set_name(fluid_sample_t*, const char*)</code> */
int fluid_sample_set_name(fluid_sample_t* sample, const char* name);
/** Original signature : <code>int fluid_sample_set_sound_data(fluid_sample_t*, short*, char*, unsigned int, unsigned int, short)</code> */
int fluid_sample_set_sound_data(fluid_sample_t* sample, short* data, char* data24, unsigned int nbframes, unsigned int sample_rate, short copy_data);
/** Original signature : <code>int fluid_sample_set_loop(fluid_sample_t*, unsigned int, unsigned int)</code> */
int fluid_sample_set_loop(fluid_sample_t* sample, unsigned int loop_start, unsigned int loop_end);
/** Original signature : <code>int fluid_sample_set_pitch(fluid_sample_t*, int, int)</code> */
int fluid_sample_set_pitch(fluid_sample_t* sample, int root_key, int fine_tune);
/**
 * Callback function type used with new_fluid_audio_driver2() to allow for<br>
 * custom user audio processing before the audio is sent to the driver. This<br>
 * function is responsible for rendering the audio to the buffers. For details<br>
 * please refer to fluid_synth_process().<br>
 * @param data The user data parameter as passed to new_fluid_audio_driver2().<br>
 * @param len Count of audio frames to synthesize.<br>
 * @param nfx Count of arrays in \c fx.<br>
 * @param fx Array of buffers to store effects audio to. Buffers may alias with buffers of \c out.<br>
 * @param nout Count of arrays in \c out.<br>
 * @param out Array of buffers to store (dry) audio to. Buffers may alias with buffers of \c fx.<br>
 * @return Should return #FLUID_OK on success, #FLUID_FAILED if an error occured.
 */
typedef int (*fluid_audio_func_t)(void* data, int len, int nfx, float* fx[], int nout, float* out[]) fluid_audio_func_t;
/** Original signature : <code>fluid_audio_driver_t* new_fluid_audio_driver(fluid_settings_t*, fluid_synth_t*)</code> */
fluid_audio_driver_t* new_fluid_audio_driver(fluid_settings_t* settings, fluid_synth_t* synth);
/** Original signature : <code>fluid_audio_driver_t* new_fluid_audio_driver2(fluid_settings_t*, fluid_audio_func_t, void*)</code> */
fluid_audio_driver_t* new_fluid_audio_driver2(fluid_settings_t* settings, fluid_audio_func_t func, void* data);
/** Original signature : <code>void delete_fluid_audio_driver(fluid_audio_driver_t*)</code> */
void delete_fluid_audio_driver(fluid_audio_driver_t* driver);
/** Original signature : <code>fluid_file_renderer_t* new_fluid_file_renderer(fluid_synth_t*)</code> */
fluid_file_renderer_t* new_fluid_file_renderer(fluid_synth_t* synth);
/** Original signature : <code>int fluid_file_renderer_process_block(fluid_file_renderer_t*)</code> */
int fluid_file_renderer_process_block(fluid_file_renderer_t* dev);
/** Original signature : <code>void delete_fluid_file_renderer(fluid_file_renderer_t*)</code> */
void delete_fluid_file_renderer(fluid_file_renderer_t* dev);
/** Original signature : <code>int fluid_file_set_encoding_quality(fluid_file_renderer_t*, double)</code> */
int fluid_file_set_encoding_quality(fluid_file_renderer_t* dev, double q);
/** Original signature : <code>int fluid_audio_driver_register(const char**)</code> */
int fluid_audio_driver_register(const char** adrivers);
/**
 * @file midi.h<br>
 * @brief Functions for MIDI events, drivers and MIDI file playback.<br>
 * Original signature : <code>fluid_midi_event_t* new_fluid_midi_event()</code>
 */
fluid_midi_event_t* new_fluid_midi_event();
/** Original signature : <code>void delete_fluid_midi_event(fluid_midi_event_t*)</code> */
void delete_fluid_midi_event(fluid_midi_event_t* event);
/** Original signature : <code>int fluid_midi_event_set_type(fluid_midi_event_t*, int)</code> */
int fluid_midi_event_set_type(fluid_midi_event_t* evt, int type);
/** Original signature : <code>int fluid_midi_event_get_type(fluid_midi_event_t*)</code> */
int fluid_midi_event_get_type(fluid_midi_event_t* evt);
/** Original signature : <code>int fluid_midi_event_set_channel(fluid_midi_event_t*, int)</code> */
int fluid_midi_event_set_channel(fluid_midi_event_t* evt, int chan);
/** Original signature : <code>int fluid_midi_event_get_channel(fluid_midi_event_t*)</code> */
int fluid_midi_event_get_channel(fluid_midi_event_t* evt);
/** Original signature : <code>int fluid_midi_event_get_key(fluid_midi_event_t*)</code> */
int fluid_midi_event_get_key(fluid_midi_event_t* evt);
/** Original signature : <code>int fluid_midi_event_set_key(fluid_midi_event_t*, int)</code> */
int fluid_midi_event_set_key(fluid_midi_event_t* evt, int key);
/** Original signature : <code>int fluid_midi_event_get_velocity(fluid_midi_event_t*)</code> */
int fluid_midi_event_get_velocity(fluid_midi_event_t* evt);
/** Original signature : <code>int fluid_midi_event_set_velocity(fluid_midi_event_t*, int)</code> */
int fluid_midi_event_set_velocity(fluid_midi_event_t* evt, int vel);
/** Original signature : <code>int fluid_midi_event_get_control(fluid_midi_event_t*)</code> */
int fluid_midi_event_get_control(fluid_midi_event_t* evt);
/** Original signature : <code>int fluid_midi_event_set_control(fluid_midi_event_t*, int)</code> */
int fluid_midi_event_set_control(fluid_midi_event_t* evt, int ctrl);
/** Original signature : <code>int fluid_midi_event_get_value(fluid_midi_event_t*)</code> */
int fluid_midi_event_get_value(fluid_midi_event_t* evt);
/** Original signature : <code>int fluid_midi_event_set_value(fluid_midi_event_t*, int)</code> */
int fluid_midi_event_set_value(fluid_midi_event_t* evt, int val);
/** Original signature : <code>int fluid_midi_event_get_program(fluid_midi_event_t*)</code> */
int fluid_midi_event_get_program(fluid_midi_event_t* evt);
/** Original signature : <code>int fluid_midi_event_set_program(fluid_midi_event_t*, int)</code> */
int fluid_midi_event_set_program(fluid_midi_event_t* evt, int val);
/** Original signature : <code>int fluid_midi_event_get_pitch(fluid_midi_event_t*)</code> */
int fluid_midi_event_get_pitch(fluid_midi_event_t* evt);
/** Original signature : <code>int fluid_midi_event_set_pitch(fluid_midi_event_t*, int)</code> */
int fluid_midi_event_set_pitch(fluid_midi_event_t* evt, int val);
/** Original signature : <code>int fluid_midi_event_set_sysex(fluid_midi_event_t*, void*, int, int)</code> */
int fluid_midi_event_set_sysex(fluid_midi_event_t* evt, void* data, int size, int dynamic);
/** Original signature : <code>int fluid_midi_event_set_text(fluid_midi_event_t*, void*, int, int)</code> */
int fluid_midi_event_set_text(fluid_midi_event_t* evt, void* data, int size, int dynamic);
/** Original signature : <code>int fluid_midi_event_get_text(fluid_midi_event_t*, void**, int*)</code> */
int fluid_midi_event_get_text(fluid_midi_event_t* evt, void** data, int* size);
/** Original signature : <code>int fluid_midi_event_set_lyrics(fluid_midi_event_t*, void*, int, int)</code> */
int fluid_midi_event_set_lyrics(fluid_midi_event_t* evt, void* data, int size, int dynamic);
/** Original signature : <code>int fluid_midi_event_get_lyrics(fluid_midi_event_t*, void**, int*)</code> */
int fluid_midi_event_get_lyrics(fluid_midi_event_t* evt, void** data, int* size);
/**
 * MIDI router rule type.<br>
 * @since 1.1.0
 */
typedef enum fluid_midi_router_rule_type {
	FLUID_MIDI_ROUTER_RULE_NOTE,
	FLUID_MIDI_ROUTER_RULE_CC,
	FLUID_MIDI_ROUTER_RULE_PROG_CHANGE,
	FLUID_MIDI_ROUTER_RULE_PITCH_BEND,
	FLUID_MIDI_ROUTER_RULE_CHANNEL_PRESSURE,
	FLUID_MIDI_ROUTER_RULE_KEY_PRESSURE,
	FLUID_MIDI_ROUTER_RULE_COUNT /**< @internal Total count of rule types @warning This symbol is not part of the public API and ABI stability guarantee and may change at any time!*/
} fluid_midi_router_rule_type;
/**
 * Generic callback function for MIDI events.<br>
 * @param data User defined data pointer<br>
 * @param event The MIDI event<br>
 * @return Should return #FLUID_OK on success, #FLUID_FAILED otherwise<br>
 * * Will be used between<br>
 * - MIDI driver and MIDI router<br>
 * - MIDI router and synth<br>
 * to communicate events.<br>
 * In the not-so-far future...
 */
typedef int (*handle_midi_event_func_t)(void* data, fluid_midi_event_t* event) handle_midi_event_func_t;
/** Original signature : <code>fluid_midi_router_t* new_fluid_midi_router(fluid_settings_t*, handle_midi_event_func_t, void*)</code> */
fluid_midi_router_t* new_fluid_midi_router(fluid_settings_t* settings, handle_midi_event_func_t handler, void* event_handler_data);
/** Original signature : <code>void delete_fluid_midi_router(fluid_midi_router_t*)</code> */
void delete_fluid_midi_router(fluid_midi_router_t* handler);
/** Original signature : <code>int fluid_midi_router_set_default_rules(fluid_midi_router_t*)</code> */
int fluid_midi_router_set_default_rules(fluid_midi_router_t* router);
/** Original signature : <code>int fluid_midi_router_clear_rules(fluid_midi_router_t*)</code> */
int fluid_midi_router_clear_rules(fluid_midi_router_t* router);
/** Original signature : <code>int fluid_midi_router_add_rule(fluid_midi_router_t*, fluid_midi_router_rule_t*, int)</code> */
int fluid_midi_router_add_rule(fluid_midi_router_t* router, fluid_midi_router_rule_t* rule, int type);
/** Original signature : <code>fluid_midi_router_rule_t* new_fluid_midi_router_rule()</code> */
fluid_midi_router_rule_t* new_fluid_midi_router_rule();
/** Original signature : <code>void delete_fluid_midi_router_rule(fluid_midi_router_rule_t*)</code> */
void delete_fluid_midi_router_rule(fluid_midi_router_rule_t* rule);
/** Original signature : <code>void fluid_midi_router_rule_set_chan(fluid_midi_router_rule_t*, int, int, float, int)</code> */
void fluid_midi_router_rule_set_chan(fluid_midi_router_rule_t* rule, int min, int max, float mul, int add);
/** Original signature : <code>void fluid_midi_router_rule_set_param1(fluid_midi_router_rule_t*, int, int, float, int)</code> */
void fluid_midi_router_rule_set_param1(fluid_midi_router_rule_t* rule, int min, int max, float mul, int add);
/** Original signature : <code>void fluid_midi_router_rule_set_param2(fluid_midi_router_rule_t*, int, int, float, int)</code> */
void fluid_midi_router_rule_set_param2(fluid_midi_router_rule_t* rule, int min, int max, float mul, int add);
/** Original signature : <code>int fluid_midi_router_handle_midi_event(void*, fluid_midi_event_t*)</code> */
int fluid_midi_router_handle_midi_event(void* data, fluid_midi_event_t* event);
/** Original signature : <code>int fluid_midi_dump_prerouter(void*, fluid_midi_event_t*)</code> */
int fluid_midi_dump_prerouter(void* data, fluid_midi_event_t* event);
/** Original signature : <code>int fluid_midi_dump_postrouter(void*, fluid_midi_event_t*)</code> */
int fluid_midi_dump_postrouter(void* data, fluid_midi_event_t* event);
/** Original signature : <code>fluid_midi_driver_t* new_fluid_midi_driver(fluid_settings_t*, handle_midi_event_func_t, void*)</code> */
fluid_midi_driver_t* new_fluid_midi_driver(fluid_settings_t* settings, handle_midi_event_func_t handler, void* event_handler_data);
/** Original signature : <code>void delete_fluid_midi_driver(fluid_midi_driver_t*)</code> */
void delete_fluid_midi_driver(fluid_midi_driver_t* driver);
/**
 * MIDI player status enum.<br>
 * @since 1.1.0
 */
enum fluid_player_status {
	FLUID_PLAYER_READY /**< Player is ready */,
	FLUID_PLAYER_PLAYING /**< Player is currently playing */,
	FLUID_PLAYER_DONE /**< Player is finished playing */
};
/** Original signature : <code>fluid_player_t* new_fluid_player(fluid_synth_t*)</code> */
fluid_player_t* new_fluid_player(fluid_synth_t* synth);
/** Original signature : <code>void delete_fluid_player(fluid_player_t*)</code> */
void delete_fluid_player(fluid_player_t* player);
/** Original signature : <code>int fluid_player_add(fluid_player_t*, const char*)</code> */
int fluid_player_add(fluid_player_t* player, const char* midifile);
/** Original signature : <code>int fluid_player_add_mem(fluid_player_t*, const void*, size_t)</code> */
int fluid_player_add_mem(fluid_player_t* player, const void* buffer, size_t len);
/** Original signature : <code>int fluid_player_play(fluid_player_t*)</code> */
int fluid_player_play(fluid_player_t* player);
/** Original signature : <code>int fluid_player_stop(fluid_player_t*)</code> */
int fluid_player_stop(fluid_player_t* player);
/** Original signature : <code>int fluid_player_join(fluid_player_t*)</code> */
int fluid_player_join(fluid_player_t* player);
/** Original signature : <code>int fluid_player_set_loop(fluid_player_t*, int)</code> */
int fluid_player_set_loop(fluid_player_t* player, int loop);
/** Original signature : <code>int fluid_player_set_midi_tempo(fluid_player_t*, int)</code> */
int fluid_player_set_midi_tempo(fluid_player_t* player, int tempo);
/** Original signature : <code>int fluid_player_set_bpm(fluid_player_t*, int)</code> */
int fluid_player_set_bpm(fluid_player_t* player, int bpm);
/** Original signature : <code>int fluid_player_set_playback_callback(fluid_player_t*, handle_midi_event_func_t, void*)</code> */
int fluid_player_set_playback_callback(fluid_player_t* player, handle_midi_event_func_t handler, void* handler_data);
/** Original signature : <code>int fluid_player_get_status(fluid_player_t*)</code> */
int fluid_player_get_status(fluid_player_t* player);
/** Original signature : <code>int fluid_player_get_current_tick(fluid_player_t*)</code> */
int fluid_player_get_current_tick(fluid_player_t* player);
/** Original signature : <code>int fluid_player_get_total_ticks(fluid_player_t*)</code> */
int fluid_player_get_total_ticks(fluid_player_t* player);
/** Original signature : <code>int fluid_player_get_bpm(fluid_player_t*)</code> */
int fluid_player_get_bpm(fluid_player_t* player);
/** Original signature : <code>int fluid_player_get_midi_tempo(fluid_player_t*)</code> */
int fluid_player_get_midi_tempo(fluid_player_t* player);
/** Original signature : <code>int fluid_player_seek(fluid_player_t*, int)</code> */
int fluid_player_seek(fluid_player_t* player, int ticks);
/** Enum used with fluid_voice_add_mod() to specify how to handle duplicate modulators. */
enum fluid_voice_add_mod {
	FLUID_VOICE_OVERWRITE /**< Overwrite any existing matching modulator */,
	FLUID_VOICE_ADD /**< Add (sum) modulator amounts */,
	FLUID_VOICE_DEFAULT /**< For default modulators only, no need to check for duplicates */
};
/** Original signature : <code>void fluid_voice_add_mod(fluid_voice_t*, fluid_mod_t*, int)</code> */
void fluid_voice_add_mod(fluid_voice_t* voice, fluid_mod_t* mod, int mode);
/** Original signature : <code>float fluid_voice_gen_get(fluid_voice_t*, int)</code> */
float fluid_voice_gen_get(fluid_voice_t* voice, int gen);
/** Original signature : <code>void fluid_voice_gen_set(fluid_voice_t*, int, float)</code> */
void fluid_voice_gen_set(fluid_voice_t* voice, int gen, float val);
/** Original signature : <code>void fluid_voice_gen_incr(fluid_voice_t*, int, float)</code> */
void fluid_voice_gen_incr(fluid_voice_t* voice, int gen, float val);
/** Original signature : <code>int fluid_voice_get_id(const fluid_voice_t*)</code> */
unsigned int fluid_voice_get_id(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_get_channel(const fluid_voice_t*)</code> */
int fluid_voice_get_channel(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_get_key(const fluid_voice_t*)</code> */
int fluid_voice_get_key(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_get_actual_key(const fluid_voice_t*)</code> */
int fluid_voice_get_actual_key(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_get_velocity(const fluid_voice_t*)</code> */
int fluid_voice_get_velocity(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_get_actual_velocity(const fluid_voice_t*)</code> */
int fluid_voice_get_actual_velocity(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_is_playing(const fluid_voice_t*)</code> */
int fluid_voice_is_playing(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_is_on(const fluid_voice_t*)</code> */
int fluid_voice_is_on(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_is_sustained(const fluid_voice_t*)</code> */
int fluid_voice_is_sustained(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_is_sostenuto(const fluid_voice_t*)</code> */
int fluid_voice_is_sostenuto(const fluid_voice_t* voice);
/** Original signature : <code>int fluid_voice_optimize_sample(fluid_sample_t*)</code> */
int fluid_voice_optimize_sample(fluid_sample_t* s);
/** Original signature : <code>void fluid_voice_update_param(fluid_voice_t*, int)</code> */
void fluid_voice_update_param(fluid_voice_t* voice, int gen);
/** Sequencer event type enumeration. */
enum fluid_seq_event_type {
	FLUID_SEQ_NOTE = 0 /**< Note event with duration */,
	FLUID_SEQ_NOTEON /**< Note on event */,
	FLUID_SEQ_NOTEOFF /**< Note off event */,
	FLUID_SEQ_ALLSOUNDSOFF /**< All sounds off event */,
	FLUID_SEQ_ALLNOTESOFF /**< All notes off event */,
	FLUID_SEQ_BANKSELECT /**< Bank select message */,
	FLUID_SEQ_PROGRAMCHANGE /**< Program change message */,
	FLUID_SEQ_PROGRAMSELECT /**< Program select message */,
	FLUID_SEQ_PITCHBEND /**< Pitch bend message */,
	FLUID_SEQ_PITCHWHEELSENS /**< Pitch wheel sensitivity set message @since 1.1.0 was mispelled previously */,
	FLUID_SEQ_MODULATION /**< Modulation controller event */,
	FLUID_SEQ_SUSTAIN /**< Sustain controller event */,
	FLUID_SEQ_CONTROLCHANGE /**< MIDI control change event */,
	FLUID_SEQ_PAN /**< Stereo pan set event */,
	FLUID_SEQ_VOLUME /**< Volume set event */,
	FLUID_SEQ_REVERBSEND /**< Reverb send set event */,
	FLUID_SEQ_CHORUSSEND /**< Chorus send set event */,
	FLUID_SEQ_TIMER /**< Timer event (useful for giving a callback at a certain time) */,
	FLUID_SEQ_ANYCONTROLCHANGE /**< Any control change message (only internally used for remove_events) */,
	FLUID_SEQ_CHANNELPRESSURE /**< Channel aftertouch event @since 1.1.0 */,
	FLUID_SEQ_KEYPRESSURE /**< Polyphonic aftertouch event @since 2.0.0 */,
	FLUID_SEQ_SYSTEMRESET /**< System reset event @since 1.1.0 */,
	FLUID_SEQ_UNREGISTERING /**< Called when a sequencer client is being unregistered. @since 1.1.0 */,
	FLUID_SEQ_LASTEVENT /**< @internal Defines the count of events enums @warning This symbol is not part of the public API and ABI stability guarantee and may change at any time! */
};
/**
 * Event alloc/free<br>
 * Original signature : <code>fluid_event_t* new_fluid_event()</code>
 */
fluid_event_t* new_fluid_event();
/** Original signature : <code>void delete_fluid_event(fluid_event_t*)</code> */
void delete_fluid_event(fluid_event_t* evt);
/**
 * Initializing events<br>
 * Original signature : <code>void fluid_event_set_source(fluid_event_t*, fluid_seq_id_t)</code>
 */
void fluid_event_set_source(fluid_event_t* evt, fluid_seq_id_t src);
/** Original signature : <code>void fluid_event_set_dest(fluid_event_t*, fluid_seq_id_t)</code> */
void fluid_event_set_dest(fluid_event_t* evt, fluid_seq_id_t dest);
/**
 * Timer events<br>
 * Original signature : <code>void fluid_event_timer(fluid_event_t*, void*)</code>
 */
void fluid_event_timer(fluid_event_t* evt, void* data);
/**
 * Note events<br>
 * Original signature : <code>void fluid_event_note(fluid_event_t*, int, short, short, unsigned int)</code>
 */
void fluid_event_note(fluid_event_t* evt, int channel, short key, short vel, unsigned int duration);
/** Original signature : <code>void fluid_event_noteon(fluid_event_t*, int, short, short)</code> */
void fluid_event_noteon(fluid_event_t* evt, int channel, short key, short vel);
/** Original signature : <code>void fluid_event_noteoff(fluid_event_t*, int, short)</code> */
void fluid_event_noteoff(fluid_event_t* evt, int channel, short key);
/** Original signature : <code>void fluid_event_all_sounds_off(fluid_event_t*, int)</code> */
void fluid_event_all_sounds_off(fluid_event_t* evt, int channel);
/** Original signature : <code>void fluid_event_all_notes_off(fluid_event_t*, int)</code> */
void fluid_event_all_notes_off(fluid_event_t* evt, int channel);
/**
 * Instrument selection<br>
 * Original signature : <code>void fluid_event_bank_select(fluid_event_t*, int, short)</code>
 */
void fluid_event_bank_select(fluid_event_t* evt, int channel, short bank_num);
/** Original signature : <code>void fluid_event_program_change(fluid_event_t*, int, short)</code> */
void fluid_event_program_change(fluid_event_t* evt, int channel, short preset_num);
/** Original signature : <code>void fluid_event_program_select(fluid_event_t*, int, unsigned int, short, short)</code> */
void fluid_event_program_select(fluid_event_t* evt, int channel, unsigned int sfont_id, short bank_num, short preset_num);
/**
 * Real-time generic instrument controllers<br>
 * Original signature : <code>void fluid_event_control_change(fluid_event_t*, int, short, short)</code>
 */
void fluid_event_control_change(fluid_event_t* evt, int channel, short control, short val);
/**
 * Real-time instrument controllers shortcuts<br>
 * Original signature : <code>void fluid_event_pitch_bend(fluid_event_t*, int, int)</code>
 */
void fluid_event_pitch_bend(fluid_event_t* evt, int channel, int val);
/** Original signature : <code>void fluid_event_pitch_wheelsens(fluid_event_t*, int, short)</code> */
void fluid_event_pitch_wheelsens(fluid_event_t* evt, int channel, short val);
/** Original signature : <code>void fluid_event_modulation(fluid_event_t*, int, short)</code> */
void fluid_event_modulation(fluid_event_t* evt, int channel, short val);
/** Original signature : <code>void fluid_event_sustain(fluid_event_t*, int, short)</code> */
void fluid_event_sustain(fluid_event_t* evt, int channel, short val);
/** Original signature : <code>void fluid_event_pan(fluid_event_t*, int, short)</code> */
void fluid_event_pan(fluid_event_t* evt, int channel, short val);
/** Original signature : <code>void fluid_event_volume(fluid_event_t*, int, short)</code> */
void fluid_event_volume(fluid_event_t* evt, int channel, short val);
/** Original signature : <code>void fluid_event_reverb_send(fluid_event_t*, int, short)</code> */
void fluid_event_reverb_send(fluid_event_t* evt, int channel, short val);
/** Original signature : <code>void fluid_event_chorus_send(fluid_event_t*, int, short)</code> */
void fluid_event_chorus_send(fluid_event_t* evt, int channel, short val);
/** Original signature : <code>void fluid_event_key_pressure(fluid_event_t*, int, short, short)</code> */
void fluid_event_key_pressure(fluid_event_t* evt, int channel, short key, short val);
/** Original signature : <code>void fluid_event_channel_pressure(fluid_event_t*, int, short)</code> */
void fluid_event_channel_pressure(fluid_event_t* evt, int channel, short val);
/** Original signature : <code>void fluid_event_system_reset(fluid_event_t*)</code> */
void fluid_event_system_reset(fluid_event_t* evt);
/**
 * Only for removing events<br>
 * Original signature : <code>void fluid_event_any_control_change(fluid_event_t*, int)</code>
 */
void fluid_event_any_control_change(fluid_event_t* evt, int channel);
/**
 * Only when unregistering clients<br>
 * Original signature : <code>void fluid_event_unregistering(fluid_event_t*)</code>
 */
void fluid_event_unregistering(fluid_event_t* evt);
/**
 * Accessing event data<br>
 * Original signature : <code>int fluid_event_get_type(fluid_event_t*)</code>
 */
int fluid_event_get_type(fluid_event_t* evt);
/** Original signature : <code>fluid_seq_id_t fluid_event_get_source(fluid_event_t*)</code> */
fluid_seq_id_t fluid_event_get_source(fluid_event_t* evt);
/** Original signature : <code>fluid_seq_id_t fluid_event_get_dest(fluid_event_t*)</code> */
fluid_seq_id_t fluid_event_get_dest(fluid_event_t* evt);
/** Original signature : <code>int fluid_event_get_channel(fluid_event_t*)</code> */
int fluid_event_get_channel(fluid_event_t* evt);
/** Original signature : <code>short FLUIDSYNTH_API fluid_event_get_key(fluid_event_t*)</code> */
short FLUIDSYNTH_API fluid_event_get_key(fluid_event_t* evt);
/** Original signature : <code>short FLUIDSYNTH_API fluid_event_get_velocity(fluid_event_t*)</code> */
short FLUIDSYNTH_API fluid_event_get_velocity(fluid_event_t* evt);
/** Original signature : <code>short FLUIDSYNTH_API fluid_event_get_control(fluid_event_t*)</code> */
short FLUIDSYNTH_API fluid_event_get_control(fluid_event_t* evt);
/** Original signature : <code>short FLUIDSYNTH_API fluid_event_get_value(fluid_event_t*)</code> */
short FLUIDSYNTH_API fluid_event_get_value(fluid_event_t* evt);
/** Original signature : <code>short FLUIDSYNTH_API fluid_event_get_program(fluid_event_t*)</code> */
short FLUIDSYNTH_API fluid_event_get_program(fluid_event_t* evt);
/** Original signature : <code>void* fluid_event_get_data(fluid_event_t*)</code> */
void* fluid_event_get_data(fluid_event_t* evt);
/** Original signature : <code>int fluid_event_get_duration(fluid_event_t*)</code> */
unsigned int fluid_event_get_duration(fluid_event_t* evt);
/** Original signature : <code>short FLUIDSYNTH_API fluid_event_get_bank(fluid_event_t*)</code> */
short FLUIDSYNTH_API fluid_event_get_bank(fluid_event_t* evt);
/** Original signature : <code>int fluid_event_get_pitch(fluid_event_t*)</code> */
int fluid_event_get_pitch(fluid_event_t* evt);
/** Original signature : <code>int fluid_event_get_sfont_id(fluid_event_t*)</code> */
unsigned int fluid_event_get_sfont_id(fluid_event_t* evt);
extern ""C"" {
/** Generator (effect) numbers (Soundfont 2.01 specifications section 8.1.3) */
	enum fluid_gen_type {
		GEN_STARTADDROFS /**< Sample start address offset (0-32767) */,
		GEN_ENDADDROFS /**< Sample end address offset (-32767-0) */,
		GEN_STARTLOOPADDROFS /**< Sample loop start address offset (-32767-32767) */,
		GEN_ENDLOOPADDROFS /**< Sample loop end address offset (-32767-32767) */,
		GEN_STARTADDRCOARSEOFS /**< Sample start address coarse offset (X 32768) */,
		GEN_MODLFOTOPITCH /**< Modulation LFO to pitch */,
		GEN_VIBLFOTOPITCH /**< Vibrato LFO to pitch */,
		GEN_MODENVTOPITCH /**< Modulation envelope to pitch */,
		GEN_FILTERFC /**< Filter cutoff */,
		GEN_FILTERQ /**< Filter Q */,
		GEN_MODLFOTOFILTERFC /**< Modulation LFO to filter cutoff */,
		GEN_MODENVTOFILTERFC /**< Modulation envelope to filter cutoff */,
		GEN_ENDADDRCOARSEOFS /**< Sample end address coarse offset (X 32768) */,
		GEN_MODLFOTOVOL /**< Modulation LFO to volume */,
		GEN_UNUSED1 /**< Unused */,
		GEN_CHORUSSEND /**< Chorus send amount */,
		GEN_REVERBSEND /**< Reverb send amount */,
		GEN_PAN /**< Stereo panning */,
		GEN_UNUSED2 /**< Unused */,
		GEN_UNUSED3 /**< Unused */,
		GEN_UNUSED4 /**< Unused */,
		GEN_MODLFODELAY /**< Modulation LFO delay */,
		GEN_MODLFOFREQ /**< Modulation LFO frequency */,
		GEN_VIBLFODELAY /**< Vibrato LFO delay */,
		GEN_VIBLFOFREQ /**< Vibrato LFO frequency */,
		GEN_MODENVDELAY /**< Modulation envelope delay */,
		GEN_MODENVATTACK /**< Modulation envelope attack */,
		GEN_MODENVHOLD /**< Modulation envelope hold */,
		GEN_MODENVDECAY /**< Modulation envelope decay */,
		GEN_MODENVSUSTAIN /**< Modulation envelope sustain */,
		GEN_MODENVRELEASE /**< Modulation envelope release */,
		GEN_KEYTOMODENVHOLD /**< Key to modulation envelope hold */,
		GEN_KEYTOMODENVDECAY /**< Key to modulation envelope decay */,
		GEN_VOLENVDELAY /**< Volume envelope delay */,
		GEN_VOLENVATTACK /**< Volume envelope attack */,
		GEN_VOLENVHOLD /**< Volume envelope hold */,
		GEN_VOLENVDECAY /**< Volume envelope decay */,
		GEN_VOLENVSUSTAIN /**< Volume envelope sustain */,
		GEN_VOLENVRELEASE /**< Volume envelope release */,
		GEN_KEYTOVOLENVHOLD /**< Key to volume envelope hold */,
		GEN_KEYTOVOLENVDECAY /**< Key to volume envelope decay */,
		GEN_INSTRUMENT /**< Instrument ID (shouldn't be set by user) */,
		GEN_RESERVED1 /**< Reserved */,
		GEN_KEYRANGE /**< MIDI note range */,
		GEN_VELRANGE /**< MIDI velocity range */,
		GEN_STARTLOOPADDRCOARSEOFS /**< Sample start loop address coarse offset (X 32768) */,
		GEN_KEYNUM /**< Fixed MIDI note number */,
		GEN_VELOCITY /**< Fixed MIDI velocity value */,
		GEN_ATTENUATION /**< Initial volume attenuation */,
		GEN_RESERVED2 /**< Reserved */,
		GEN_ENDLOOPADDRCOARSEOFS /**< Sample end loop address coarse offset (X 32768) */,
		GEN_COARSETUNE /**< Coarse tuning */,
		GEN_FINETUNE /**< Fine tuning */,
		GEN_SAMPLEID /**< Sample ID (shouldn't be set by user) */,
		GEN_SAMPLEMODE /**< Sample mode flags */,
		GEN_RESERVED3 /**< Reserved */,
		GEN_SCALETUNE /**< Scale tuning */,
		GEN_EXCLUSIVECLASS /**< Exclusive class number */,
		GEN_OVERRIDEROOTKEY /**< Sample root note override */,
		GEN_PITCH /**< Pitch @note Not a real SoundFont generator */,
		GEN_CUSTOM_BALANCE /**< Balance @note Not a real SoundFont generator */,
		GEN_CUSTOM_FILTERFC /**< Custom filter cutoff frequency */,
		GEN_CUSTOM_FILTERQ /**< Custom filter Q */,
		GEN_LAST /**< @internal Value defines the count of generators (#fluid_gen_type) @warning This symbol is not part of the public API and ABI stability guarantee and may change at any time! */
	};
}
/**
 * @file ladspa.h<br>
 * @brief Functions for manipulating the ladspa effects unit<br>
 * * This header defines useful functions for programatically manipulating the ladspa<br>
 * effects unit of the synth that can be retrieved via fluid_synth_get_ladspa_fx().<br>
 * * Using any of those functions requires fluidsynth to be compiled with ladspa support.<br>
 * Else all of those functions are useless dummies.<br>
 * Original signature : <code>int fluid_ladspa_is_active(fluid_ladspa_fx_t*)</code>
 */
int fluid_ladspa_is_active(fluid_ladspa_fx_t* fx);
/** Original signature : <code>int fluid_ladspa_activate(fluid_ladspa_fx_t*)</code> */
int fluid_ladspa_activate(fluid_ladspa_fx_t* fx);
/** Original signature : <code>int fluid_ladspa_deactivate(fluid_ladspa_fx_t*)</code> */
int fluid_ladspa_deactivate(fluid_ladspa_fx_t* fx);
/** Original signature : <code>int fluid_ladspa_reset(fluid_ladspa_fx_t*)</code> */
int fluid_ladspa_reset(fluid_ladspa_fx_t* fx);
/** Original signature : <code>int fluid_ladspa_check(fluid_ladspa_fx_t*, char*, int)</code> */
int fluid_ladspa_check(fluid_ladspa_fx_t* fx, char* err, int err_size);
/** Original signature : <code>int fluid_ladspa_host_port_exists(fluid_ladspa_fx_t*, const char*)</code> */
int fluid_ladspa_host_port_exists(fluid_ladspa_fx_t* fx, const char* name);
/** Original signature : <code>int fluid_ladspa_add_buffer(fluid_ladspa_fx_t*, const char*)</code> */
int fluid_ladspa_add_buffer(fluid_ladspa_fx_t* fx, const char* name);
/** Original signature : <code>int fluid_ladspa_buffer_exists(fluid_ladspa_fx_t*, const char*)</code> */
int fluid_ladspa_buffer_exists(fluid_ladspa_fx_t* fx, const char* name);
/** Original signature : <code>int fluid_ladspa_add_effect(fluid_ladspa_fx_t*, const char*, const char*, const char*)</code> */
int fluid_ladspa_add_effect(fluid_ladspa_fx_t* fx, const char* effect_name, const char* lib_name, const char* plugin_name);
/** Original signature : <code>int fluid_ladspa_effect_can_mix(fluid_ladspa_fx_t*, const char*)</code> */
int fluid_ladspa_effect_can_mix(fluid_ladspa_fx_t* fx, const char* name);
/** Original signature : <code>int fluid_ladspa_effect_set_mix(fluid_ladspa_fx_t*, const char*, int, float)</code> */
int fluid_ladspa_effect_set_mix(fluid_ladspa_fx_t* fx, const char* name, int mix, float gain);
/** Original signature : <code>int fluid_ladspa_effect_port_exists(fluid_ladspa_fx_t*, const char*, const char*)</code> */
int fluid_ladspa_effect_port_exists(fluid_ladspa_fx_t* fx, const char* effect_name, const char* port_name);
/** Original signature : <code>int fluid_ladspa_effect_set_control(fluid_ladspa_fx_t*, const char*, const char*, float)</code> */
int fluid_ladspa_effect_set_control(fluid_ladspa_fx_t* fx, const char* effect_name, const char* port_name, float val);
/** Original signature : <code>int fluid_ladspa_effect_link(fluid_ladspa_fx_t*, const char*, const char*, const char*)</code> */
int fluid_ladspa_effect_link(fluid_ladspa_fx_t* fx, const char* effect_name, const char* port_name, const char* name);
/** FluidSynth log levels. */
enum fluid_log_level {
	FLUID_PANIC /**< The synth can't function correctly any more */,
	FLUID_ERR /**< Serious error occurred */,
	FLUID_WARN /**< Warning */,
	FLUID_INFO /**< Verbose informational messages */,
	FLUID_DBG /**< Debugging messages */,
	LAST_LOG_LEVEL /**< @warning This symbol is not part of the public API and ABI stability guarantee and may change at any time! */
};
/**
 * Log function handler callback type used by fluid_set_log_function().<br>
 * @param level Log level (#fluid_log_level)<br>
 * @param message Log message text<br>
 * @param data User data pointer supplied to fluid_set_log_function().
 */
typedef void (*fluid_log_function_t)(int level, const char* message, void* data) fluid_log_function_t;
/** Original signature : <code>fluid_log_function_t fluid_set_log_function(int, fluid_log_function_t, void*)</code> */
fluid_log_function_t fluid_set_log_function(int level, fluid_log_function_t fun, void* data);
/** Original signature : <code>void fluid_default_log_function(int, const char*, void*)</code> */
void fluid_default_log_function(int level, const char* message, void* data);
/** Original signature : <code>int fluid_log(int, const char*, null)</code> */
int fluid_log(int level, const char* fmt, ...);
/**
 * Value that indicates failure, used by most libfluidsynth functions.<br>
 * @since 1.1.0<br>
 * * @note See #FLUID_OK for more details.<br>
 * Original signature : <code>int fluid_is_soundfont(const char*)</code>
 */
int fluid_is_soundfont(const char* filename);
/** Original signature : <code>int fluid_is_midifile(const char*)</code> */
int fluid_is_midifile(const char* filename);
/**
 * Flags defining the polarity, mapping function and type of a modulator source.<br>
 * Compare with SoundFont 2.04 PDF section 8.2.<br>
 * Note: Bit values do not correspond to the SoundFont spec!  Also note that<br>
 * #FLUID_MOD_GC and #FLUID_MOD_CC are in the flags field instead of the source field.
 */
enum fluid_mod_flags {
	FLUID_MOD_POSITIVE = 0 /**< Mapping function is positive */,
	FLUID_MOD_NEGATIVE = 1 /**< Mapping function is negative */,
	FLUID_MOD_UNIPOLAR = 0 /**< Mapping function is unipolar */,
	FLUID_MOD_BIPOLAR = 2 /**< Mapping function is bipolar */,
	FLUID_MOD_LINEAR = 0 /**< Linear mapping function */,
	FLUID_MOD_CONCAVE = 4 /**< Concave mapping function */,
	FLUID_MOD_CONVEX = 8 /**< Convex mapping function */,
	FLUID_MOD_SWITCH = 12 /**< Switch (on/off) mapping function */,
	FLUID_MOD_GC = 0 /**< General controller source type (#fluid_mod_src) */,
	FLUID_MOD_CC = 16 /**< MIDI CC controller (source will be a MIDI CC number) */,
	FLUID_MOD_SIN = 0x80 /**< Custom non-standard sinus mapping function */
};
/**
 * General controller (if #FLUID_MOD_GC in flags).  This<br>
 * corresponds to SoundFont 2.04 PDF section 8.2.1
 */
enum fluid_mod_src {
	FLUID_MOD_NONE = 0 /**< No source controller */,
	FLUID_MOD_VELOCITY = 2 /**< MIDI note-on velocity */,
	FLUID_MOD_KEY = 3 /**< MIDI note-on note number */,
	FLUID_MOD_KEYPRESSURE = 10 /**< MIDI key pressure */,
	FLUID_MOD_CHANNELPRESSURE = 13 /**< MIDI channel pressure */,
	FLUID_MOD_PITCHWHEEL = 14 /**< Pitch wheel */,
	FLUID_MOD_PITCHWHEELSENS = 16 /**< Pitch wheel sensitivity */
};
/** Original signature : <code>fluid_mod_t* new_fluid_mod()</code> */
fluid_mod_t* new_fluid_mod();
/** Original signature : <code>void delete_fluid_mod(fluid_mod_t*)</code> */
void delete_fluid_mod(fluid_mod_t* mod);
/** Original signature : <code>size_t fluid_mod_sizeof()</code> */
size_t fluid_mod_sizeof();
/** Original signature : <code>void fluid_mod_set_source1(fluid_mod_t*, int, int)</code> */
void fluid_mod_set_source1(fluid_mod_t* mod, int src, int flags);
/** Original signature : <code>void fluid_mod_set_source2(fluid_mod_t*, int, int)</code> */
void fluid_mod_set_source2(fluid_mod_t* mod, int src, int flags);
/** Original signature : <code>void fluid_mod_set_dest(fluid_mod_t*, int)</code> */
void fluid_mod_set_dest(fluid_mod_t* mod, int dst);
/** Original signature : <code>void fluid_mod_set_amount(fluid_mod_t*, double)</code> */
void fluid_mod_set_amount(fluid_mod_t* mod, double amount);
/** Original signature : <code>int fluid_mod_get_source1(const fluid_mod_t*)</code> */
int fluid_mod_get_source1(const fluid_mod_t* mod);
/** Original signature : <code>int fluid_mod_get_flags1(const fluid_mod_t*)</code> */
int fluid_mod_get_flags1(const fluid_mod_t* mod);
/** Original signature : <code>int fluid_mod_get_source2(const fluid_mod_t*)</code> */
int fluid_mod_get_source2(const fluid_mod_t* mod);
/** Original signature : <code>int fluid_mod_get_flags2(const fluid_mod_t*)</code> */
int fluid_mod_get_flags2(const fluid_mod_t* mod);
/** Original signature : <code>int fluid_mod_get_dest(const fluid_mod_t*)</code> */
int fluid_mod_get_dest(const fluid_mod_t* mod);
/** Original signature : <code>double fluid_mod_get_amount(const fluid_mod_t*)</code> */
double fluid_mod_get_amount(const fluid_mod_t* mod);
/** Original signature : <code>int fluid_mod_test_identity(const fluid_mod_t*, const fluid_mod_t*)</code> */
int fluid_mod_test_identity(const fluid_mod_t* mod1, const fluid_mod_t* mod2);
/** Original signature : <code>int fluid_mod_has_source(const fluid_mod_t*, int, int)</code> */
int fluid_mod_has_source(const fluid_mod_t* mod, int cc, int ctrl);
/** Original signature : <code>int fluid_mod_has_dest(const fluid_mod_t*, int)</code> */
int fluid_mod_has_dest(const fluid_mod_t* mod, int gen);
/** Original signature : <code>void fluid_mod_clone(fluid_mod_t*, const fluid_mod_t*)</code> */
void fluid_mod_clone(fluid_mod_t* mod, const fluid_mod_t* src);

/**
 * Event callback prototype for destination clients.<br>
 * @param time Current sequencer tick value (see fluid_sequencer_get_tick()).<br>
 * @param event The event being received<br>
 * @param seq The sequencer instance<br>
 * @param data User defined data registered with the client
 */
typedef void (*fluid_event_callback_t)(unsigned int time, fluid_event_t* event, fluid_sequencer_t* seq, void* data) fluid_event_callback_t;
/** Original signature : <code>fluid_sequencer_t* new_fluid_sequencer()</code> */
fluid_sequencer_t* new_fluid_sequencer();
/** Original signature : <code>fluid_sequencer_t* new_fluid_sequencer2(int)</code> */
fluid_sequencer_t* new_fluid_sequencer2(int use_system_timer);
/** Original signature : <code>void delete_fluid_sequencer(fluid_sequencer_t*)</code> */
void delete_fluid_sequencer(fluid_sequencer_t* seq);
/** Original signature : <code>int fluid_sequencer_get_use_system_timer(fluid_sequencer_t*)</code> */
int fluid_sequencer_get_use_system_timer(fluid_sequencer_t* seq);
/** Original signature : <code>fluid_seq_id_t fluid_sequencer_register_client(fluid_sequencer_t*, const char*, fluid_event_callback_t, void*)</code> */
fluid_seq_id_t fluid_sequencer_register_client(fluid_sequencer_t* seq, const char* name, fluid_event_callback_t callback, void* data);
/** Original signature : <code>void fluid_sequencer_unregister_client(fluid_sequencer_t*, fluid_seq_id_t)</code> */
void fluid_sequencer_unregister_client(fluid_sequencer_t* seq, fluid_seq_id_t id);
/** Original signature : <code>int fluid_sequencer_count_clients(fluid_sequencer_t*)</code> */
int fluid_sequencer_count_clients(fluid_sequencer_t* seq);
/** Original signature : <code>fluid_seq_id_t fluid_sequencer_get_client_id(fluid_sequencer_t*, int)</code> */
fluid_seq_id_t fluid_sequencer_get_client_id(fluid_sequencer_t* seq, int index);
/** Original signature : <code>char* fluid_sequencer_get_client_name(fluid_sequencer_t*, fluid_seq_id_t)</code> */
char* fluid_sequencer_get_client_name(fluid_sequencer_t* seq, fluid_seq_id_t id);
/** Original signature : <code>int fluid_sequencer_client_is_dest(fluid_sequencer_t*, fluid_seq_id_t)</code> */
int fluid_sequencer_client_is_dest(fluid_sequencer_t* seq, fluid_seq_id_t id);
/** Original signature : <code>void fluid_sequencer_process(fluid_sequencer_t*, unsigned int)</code> */
void fluid_sequencer_process(fluid_sequencer_t* seq, unsigned int msec);
/** Original signature : <code>void fluid_sequencer_send_now(fluid_sequencer_t*, fluid_event_t*)</code> */
void fluid_sequencer_send_now(fluid_sequencer_t* seq, fluid_event_t* evt);
/** Original signature : <code>int fluid_sequencer_send_at(fluid_sequencer_t*, fluid_event_t*, unsigned int, int)</code> */
int fluid_sequencer_send_at(fluid_sequencer_t* seq, fluid_event_t* evt, unsigned int time, int absolute);
/** Original signature : <code>void fluid_sequencer_remove_events(fluid_sequencer_t*, fluid_seq_id_t, fluid_seq_id_t, int)</code> */
void fluid_sequencer_remove_events(fluid_sequencer_t* seq, fluid_seq_id_t source, fluid_seq_id_t dest, int type);
/** Original signature : <code>int fluid_sequencer_get_tick(fluid_sequencer_t*)</code> */
unsigned int fluid_sequencer_get_tick(fluid_sequencer_t* seq);
/** Original signature : <code>void fluid_sequencer_set_time_scale(fluid_sequencer_t*, double)</code> */
void fluid_sequencer_set_time_scale(fluid_sequencer_t* seq, double scale);
/** Original signature : <code>double fluid_sequencer_get_time_scale(fluid_sequencer_t*)</code> */
double fluid_sequencer_get_time_scale(fluid_sequencer_t* seq);
/**
 * @file seqbind.h<br>
 * @brief Functions for binding sequencer objects to other subsystems.<br>
 * Original signature : <code>fluid_seq_id_t fluid_sequencer_register_fluidsynth(fluid_sequencer_t*, fluid_synth_t*)</code>
 */
fluid_seq_id_t fluid_sequencer_register_fluidsynth(fluid_sequencer_t* seq, fluid_synth_t* synth);
/** Original signature : <code>int fluid_sequencer_add_midi_event_to_buffer(void*, fluid_midi_event_t*)</code> */
int fluid_sequencer_add_midi_event_to_buffer(void* data, fluid_midi_event_t* event);
/**
 * Settings type<br>
 * Each setting has a defined type: numeric (double), integer, string or a<br>
 * set of values. The type of each setting can be retrieved using the<br>
 * function fluid_settings_get_type()
 */
enum fluid_types_enum {
	FLUID_NO_TYPE = -1 /**< Undefined type */,
	FLUID_NUM_TYPE /**< Numeric (double) */,
	FLUID_INT_TYPE /**< Integer */,
	FLUID_STR_TYPE /**< String */,
	FLUID_SET_TYPE /**< Set of values */
};
/** Original signature : <code>fluid_settings_t* new_fluid_settings()</code> */
fluid_settings_t* new_fluid_settings();
/** Original signature : <code>void delete_fluid_settings(fluid_settings_t*)</code> */
void delete_fluid_settings(fluid_settings_t* settings);
/** Original signature : <code>int fluid_settings_get_type(fluid_settings_t*, const char*)</code> */
int fluid_settings_get_type(fluid_settings_t* settings, const char* name);
/** Original signature : <code>int fluid_settings_get_hints(fluid_settings_t*, const char*, int*)</code> */
int fluid_settings_get_hints(fluid_settings_t* settings, const char* name, int* val);
/** Original signature : <code>int fluid_settings_is_realtime(fluid_settings_t*, const char*)</code> */
int fluid_settings_is_realtime(fluid_settings_t* settings, const char* name);
/** Original signature : <code>int fluid_settings_setstr(fluid_settings_t*, const char*, const char*)</code> */
int fluid_settings_setstr(fluid_settings_t* settings, const char* name, const char* str);
/** Original signature : <code>int fluid_settings_copystr(fluid_settings_t*, const char*, char*, int)</code> */
int fluid_settings_copystr(fluid_settings_t* settings, const char* name, char* str, int len);
/** Original signature : <code>int fluid_settings_dupstr(fluid_settings_t*, const char*, char**)</code> */
int fluid_settings_dupstr(fluid_settings_t* settings, const char* name, char** str);
/** Original signature : <code>int fluid_settings_getstr_default(fluid_settings_t*, const char*, char**)</code> */
int fluid_settings_getstr_default(fluid_settings_t* settings, const char* name, char** def);
/** Original signature : <code>int fluid_settings_str_equal(fluid_settings_t*, const char*, const char*)</code> */
int fluid_settings_str_equal(fluid_settings_t* settings, const char* name, const char* value);
/** Original signature : <code>int fluid_settings_setnum(fluid_settings_t*, const char*, double)</code> */
int fluid_settings_setnum(fluid_settings_t* settings, const char* name, double val);
/** Original signature : <code>int fluid_settings_getnum(fluid_settings_t*, const char*, double*)</code> */
int fluid_settings_getnum(fluid_settings_t* settings, const char* name, double* val);
/** Original signature : <code>int fluid_settings_getnum_default(fluid_settings_t*, const char*, double*)</code> */
int fluid_settings_getnum_default(fluid_settings_t* settings, const char* name, double* val);
/** Original signature : <code>int fluid_settings_getnum_range(fluid_settings_t*, const char*, double*, double*)</code> */
int fluid_settings_getnum_range(fluid_settings_t* settings, const char* name, double* min, double* max);
/** Original signature : <code>int fluid_settings_setint(fluid_settings_t*, const char*, int)</code> */
int fluid_settings_setint(fluid_settings_t* settings, const char* name, int val);
/** Original signature : <code>int fluid_settings_getint(fluid_settings_t*, const char*, int*)</code> */
int fluid_settings_getint(fluid_settings_t* settings, const char* name, int* val);
/** Original signature : <code>int fluid_settings_getint_default(fluid_settings_t*, const char*, int*)</code> */
int fluid_settings_getint_default(fluid_settings_t* settings, const char* name, int* val);
/** Original signature : <code>int fluid_settings_getint_range(fluid_settings_t*, const char*, int*, int*)</code> */
int fluid_settings_getint_range(fluid_settings_t* settings, const char* name, int* min, int* max);
/**
 * Callback function type used with fluid_settings_foreach_option()<br>
 * @param data User defined data pointer<br>
 * @param name Setting name<br>
 * @param option A string option for this setting (iterates through the list)
 */
typedef void (*fluid_settings_foreach_option_t)(void* data, const char* name, const char* option) fluid_settings_foreach_option_t;
/** Original signature : <code>void fluid_settings_foreach_option(fluid_settings_t*, const char*, void*, fluid_settings_foreach_option_t)</code> */
void fluid_settings_foreach_option(fluid_settings_t* settings, const char* name, void* data, fluid_settings_foreach_option_t func);
/** Original signature : <code>int fluid_settings_option_count(fluid_settings_t*, const char*)</code> */
int fluid_settings_option_count(fluid_settings_t* settings, const char* name);
/** Original signature : <code>char* fluid_settings_option_concat(fluid_settings_t*, const char*, const char*)</code> */
char* fluid_settings_option_concat(fluid_settings_t* settings, const char* name, const char* separator);
/**
 * Callback function type used with fluid_settings_foreach()<br>
 * @param data User defined data pointer<br>
 * @param name Setting name<br>
 * @param type Setting type (#fluid_types_enum)
 */
typedef void (*fluid_settings_foreach_t)(void* data, const char* name, int type) fluid_settings_foreach_t;
/** Original signature : <code>void fluid_settings_foreach(fluid_settings_t*, void*, fluid_settings_foreach_t)</code> */
void fluid_settings_foreach(fluid_settings_t* settings, void* data, fluid_settings_foreach_t func);
/**
 * @file shell.h<br>
 * @brief Command shell interface<br>
 * * The shell interface allows you to send simple textual commands to<br>
 * the synthesizer, to parse a command file, or to read commands<br>
 * from the stdin or other input streams.<br>
 * Original signature : <code>fluid_istream_t fluid_get_stdin()</code>
 */
fluid_istream_t fluid_get_stdin();
/** Original signature : <code>fluid_ostream_t fluid_get_stdout()</code> */
fluid_ostream_t fluid_get_stdout();
/** Original signature : <code>char* fluid_get_userconf(char*, int)</code> */
char* fluid_get_userconf(char* buf, int len);
/** Original signature : <code>char* fluid_get_sysconf(char*, int)</code> */
char* fluid_get_sysconf(char* buf, int len);
/**
 * The command handler<br>
 * Original signature : <code>fluid_cmd_handler_t* new_fluid_cmd_handler(fluid_synth_t*, fluid_midi_router_t*)</code>
 */
fluid_cmd_handler_t* new_fluid_cmd_handler(fluid_synth_t* synth, fluid_midi_router_t* router);
/** Original signature : <code>void delete_fluid_cmd_handler(fluid_cmd_handler_t*)</code> */
void delete_fluid_cmd_handler(fluid_cmd_handler_t* handler);
/** Original signature : <code>void fluid_cmd_handler_set_synth(fluid_cmd_handler_t*, fluid_synth_t*)</code> */
void fluid_cmd_handler_set_synth(fluid_cmd_handler_t* handler, fluid_synth_t* synth);
/**
 * Command function<br>
 * Original signature : <code>int fluid_command(fluid_cmd_handler_t*, const char*, fluid_ostream_t)</code>
 */
int fluid_command(fluid_cmd_handler_t* handler, const char* cmd, fluid_ostream_t out);
/** Original signature : <code>int fluid_source(fluid_cmd_handler_t*, const char*)</code> */
int fluid_source(fluid_cmd_handler_t* handler, const char* filename);
/** Original signature : <code>void fluid_usershell(fluid_settings_t*, fluid_cmd_handler_t*)</code> */
void fluid_usershell(fluid_settings_t* settings, fluid_cmd_handler_t* handler);
/**
 * Shell<br>
 * Original signature : <code>fluid_shell_t* new_fluid_shell(fluid_settings_t*, fluid_cmd_handler_t*, fluid_istream_t, fluid_ostream_t, int)</code>
 */
fluid_shell_t* new_fluid_shell(fluid_settings_t* settings, fluid_cmd_handler_t* handler, fluid_istream_t in, fluid_ostream_t out, int thread);
/** Original signature : <code>void delete_fluid_shell(fluid_shell_t*)</code> */
void delete_fluid_shell(fluid_shell_t* shell);
/**
 * TCP/IP server<br>
 * Original signature : <code>fluid_server_t* new_fluid_server(fluid_settings_t*, fluid_synth_t*, fluid_midi_router_t*)</code>
 */
fluid_server_t* new_fluid_server(fluid_settings_t* settings, fluid_synth_t* synth, fluid_midi_router_t* router);
/** Original signature : <code>void delete_fluid_server(fluid_server_t*)</code> */
void delete_fluid_server(fluid_server_t* server);
/** Original signature : <code>int fluid_server_join(fluid_server_t*)</code> */
int fluid_server_join(fluid_server_t* server);
extern ""C"" {
/**
	 * @file types.h<br>
	 * @brief Type declarations
	 */
	typedef _fluid_hashtable_t fluid_settings_t; /**< Configuration settings instance */
	typedef _fluid_synth_t fluid_synth_t; /**< Synthesizer instance */
	typedef _fluid_voice_t fluid_voice_t; /**< Synthesis voice instance */
	typedef _fluid_sfloader_t fluid_sfloader_t; /**< SoundFont loader plugin */
	typedef _fluid_sfont_t fluid_sfont_t; /**< SoundFont */
	typedef _fluid_preset_t fluid_preset_t; /**< SoundFont preset */
	typedef _fluid_sample_t fluid_sample_t; /**< SoundFont sample */
	typedef _fluid_mod_t fluid_mod_t; /**< SoundFont modulator */
	typedef _fluid_audio_driver_t fluid_audio_driver_t; /**< Audio driver instance */
	typedef _fluid_file_renderer_t fluid_file_renderer_t; /**< Audio file renderer instance */
	typedef _fluid_player_t fluid_player_t; /**< MIDI player instance */
	typedef _fluid_midi_event_t fluid_midi_event_t; /**< MIDI event */
	typedef _fluid_midi_driver_t fluid_midi_driver_t; /**< MIDI driver instance */
	typedef _fluid_midi_router_t fluid_midi_router_t; /**< MIDI router instance */
	typedef _fluid_midi_router_rule_t fluid_midi_router_rule_t; /**< MIDI router rule */
	typedef _fluid_hashtable_t fluid_cmd_hash_t; /**< Command handler hash table */
	typedef _fluid_shell_t fluid_shell_t; /**< Command shell */
	typedef _fluid_server_t fluid_server_t; /**< TCP/IP shell server instance */
	typedef _fluid_event_t fluid_event_t; /**< Sequencer event */
	typedef _fluid_sequencer_t fluid_sequencer_t; /**< Sequencer instance */
	typedef _fluid_ramsfont_t fluid_ramsfont_t; /**< RAM SoundFont */
	typedef _fluid_rampreset_t fluid_rampreset_t; /**< RAM SoundFont preset */
	typedef _fluid_cmd_handler_t fluid_cmd_handler_t; /**< Shell Command Handler */
	typedef _fluid_ladspa_fx_t fluid_ladspa_fx_t; /**< LADSPA effects instance */
	typedef _fluid_file_callbacks_t fluid_file_callbacks_t; /**< Callback struct to perform custom file loading of soundfonts */
	typedef int fluid_istream_t; /**< Input stream descriptor */
	typedef int fluid_ostream_t; /**< Output stream descriptor */
	typedef short fluid_seq_id_t; /**< Unique client IDs used by the sequencer and #fluid_event_t, obtained by fluid_sequencer_register_client() and fluid_sequencer_register_fluidsynth() */
}
