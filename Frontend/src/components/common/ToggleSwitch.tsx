import React from 'react';

interface ToggleSwitchProps {
  isOn: boolean;
  handleToggle: () => void;
  onColor?: string;
  offColor?: string;
}

const ToggleSwitch: React.FC<ToggleSwitchProps> = ({
  isOn,
  handleToggle,
  onColor = 'bg-green-500',
  offColor = 'bg-red-500',
}) => {
  return (
    <div
      className={`relative inline-flex items-center h-6 rounded-full w-11 cursor-pointer transition-colors duration-200 ease-in-out ${isOn ? onColor : offColor}`}
      onClick={handleToggle}
    >
      <span
        className={`transform transition-transform duration-200 ease-in-out inline-block w-5 h-5 bg-white rounded-full shadow-md ${isOn ? 'translate-x-5' : 'translate-x-0.5'}`}
      />
    </div>
  );
};

export default ToggleSwitch;